package com.chatbot.adjustment.service;

import com.chatbot.adjustment.batch.service.ChatApiService;
import com.chatbot.adjustment.kafka.domain.CdrUnit;
import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import com.chatbot.adjustment.web.adjustment.domain.CdrHis;
import com.chatbot.adjustment.web.adjustment.repository.BillingStatRepository;
import com.chatbot.adjustment.web.adjustment.repository.CdrHisRepository;
import com.chatbot.adjustment.web.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BillingStatService {

	private static final String DEFAULT_STAT_DATE = "2018010100"; //기본 통계 시작 일짜
	private static final String STAT_DATE_PATTERN = "yyyyMMddHH"; //통계 날짜 패턴


	@Autowired
	BillingStatRepository billingStatRepository;

	@Autowired
	CdrHisRepository cdrHisRepository;

	@Autowired
	ChatApiService chatApiService;

	@Autowired
	MallService mallService;

	/**
	 * 마지막 통계 기록 날짜
	 * @return
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public String getMaxStatDate() {
		String date = DEFAULT_STAT_DATE;

		Object[] result = billingStatRepository.getMaxStatDate();
		if(result != null && result.length > 0) {
			date = String.valueOf(result[0]);
		} else {

		}
		return date;
	}

	@Transactional(value = "adjustmentTransactionManager")
	public BillingStat get(long mallID, String date) {

		return billingStatRepository.findByMallAndDate(mallID, date);
	}

	/**
	 * 해당 빌링 통계의 채팅 CDR 카운트를 업데이트한다.
	 * @param statDate
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void updateChatCdrCount(String statDate, List<Map<Long, Integer>> list) {

		if(list.size() == 0)
			return;

		long mallID;
		int count;

		List<BillingStat> stats = new ArrayList<>();

		for(Map<Long, Integer> map : list) {
			for (Map.Entry<Long, Integer> entry : map.entrySet()) {
				mallID = entry.getKey();
				count = entry.getValue();

				BillingStat billingStat = get(mallID, statDate);
				if (billingStat == null) {
					billingStat = new BillingStat(mallID, statDate);
				}

				billingStat.setChat_cdr_count(count);

				stats.add(billingStat);
			}
		}

		billingStatRepository.save(stats);
	}


	/**
	 * 해당 통계 재정산 처리
	 * @param id
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void reAdjustment(long id) {

		BillingStat billingStat = billingStatRepository.findOne(id);

		if(billingStat == null)
			return;

		String statDate = billingStat.getDate();

		String from = statDate + "00";
		String to = DateUtils.getDateTimeAdd(statDate, STAT_DATE_PATTERN, 1, ChronoUnit.HOURS) + "00";

		//재정산을 위해 chat server에서 cdr 목록을 가져온다.
		List<CdrUnit> cdrUnits = chatApiService.getList(from, to);
		List<CdrUnit> addUnits = new ArrayList<>();

		//기존에 이미 있는 cdr인지 체크해서 걸러낸다.
		for(CdrUnit cdrUnit : cdrUnits) {

			CdrHis his = cdrHisRepository.findByMallAndSession(cdrUnit.getMall(), cdrUnit.getSession());
			if(his == null)
				addUnits.add(cdrUnit);
		}
		//신규 cdr 처리
		mallService.consumeCash(addUnits);
	}

}
