package com.chatbot.adjustment.service;

import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import com.chatbot.adjustment.web.adjustment.repository.BillingStatRepository;
import com.chatbot.adjustment.web.common.excel.ExcelWriteComponent;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.domain.code.UserSearchType;
import com.chatbot.adjustment.web.login.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AdminService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BillingStatRepository billingStatRepository;

	@Autowired
	HomepageService homepageService;

	@Autowired
	UserService userService;

	public long searchMall(UserSearchType searchType, String searchValue) {

		long mallID = 0;

		if(searchType == UserSearchType.USER_NAME) {
			User user = userService.getByUserName(searchValue);
			if(user != null)
				mallID = user.getMall_id();
		} else {
			TbUser tbUser = homepageService.getSbUser(searchType, searchValue);
			if(tbUser != null) {
				mallID = tbUser.getSso_mall_id();
			}
		}

		return mallID;
	}

	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> search(String fromDate, String toDate, long mallID) {

		if(fromDate != null && fromDate.isEmpty() == false) {
			fromDate = fromDate.replace(".", "");
		}

		if(toDate != null && toDate.isEmpty() == false) {
			toDate = toDate.replace(".", "");
		}

		List<BillingStat> list = new ArrayList<>();
		if(mallID > 0 )
			list = billingStatRepository.search(fromDate, toDate, mallID);
		else
			list = billingStatRepository.search(fromDate, toDate);

		return list;
	}

	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> search(String ym, long mallID) {

		List<BillingStat> list = new ArrayList<>();
		if(mallID > 0)
			list = billingStatRepository.searchMonth(ym, mallID);
		else
			list = billingStatRepository.searchMonth(ym);

		return list;
	}

	public Map<String, Object> makeExcelContents(List<BillingStat> contents) {
		Map<String, Object> map = new HashMap<>();
		map.put(ExcelWriteComponent.FILE_NAME, "default_excel");
		map.put(ExcelWriteComponent.HEAD,
				Arrays.asList("구분",
						"결제현황-전체", "결제현황-전자결제", "결제현황-무통장입금",
						"캐시 충전 금액-전체", "캐시 충전 금액-무료", "캐시 충전 금액-유료",
						"캐시 차감 금액-전체", "캐시 차감 금액-무료", "캐시 차감 금액-유료",
						"캐시 잔액-전체", "캐시 잔액-무료", "캐시 잔액-유료"));

		List<List<String>> list = new ArrayList<>();
		for(BillingStat stat : contents) {
			list.add(stat.getList());
		}
		map.put(ExcelWriteComponent.BODY, list);

		return map;
	}


	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> searchForMall(String fromDate, String toDate) {

		if(fromDate != null && fromDate.isEmpty() == false) {
			fromDate = fromDate.replace(".", "");
		}

		if(toDate != null && toDate.isEmpty() == false) {
			toDate = toDate.replace(".", "");
		}

		List<BillingStat> list = billingStatRepository.searchForMall(fromDate, toDate);

		addMallNameForList(list);

		return list;
	}

	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> searchForMall(String ym) {

		List<BillingStat> list = billingStatRepository.searchMonthForMall(ym);

		addMallNameForList(list);

		return list;
	}

	public Map<String, Object> makeExcelContentsForMall(List<BillingStat> contents) {
		Map<String, Object> map = new HashMap<>();
		map.put(ExcelWriteComponent.FILE_NAME, "default_excel");
		map.put(ExcelWriteComponent.HEAD,
				Arrays.asList("업체명",
						"결제현황-전체", "결제현황-전자결제", "결제현황-무통장입금",
						"캐시 충전 금액-전체", "캐시 충전 금액-무료", "캐시 충전 금액-유료",
						"캐시 차감 금액-전체", "캐시 차감 금액-무료", "캐시 차감 금액-유료",
						"캐시 잔액-전체", "캐시 잔액-무료", "캐시 잔액-유료"));

		List<List<String>> list = new ArrayList<>();
		for(BillingStat stat : contents) {
			list.add(stat.getList());
		}
		map.put(ExcelWriteComponent.BODY, list);

		return map;
	}

	private void addMallNameForList(List<BillingStat> statList) {

		List<Long> mallIds = new ArrayList<>();
		for(BillingStat stat : statList) {
			mallIds.add(stat.getMall_id());
			logger.debug("stat mall id : " + stat.getMall_id());
		}

		if(mallIds.size() > 0) {
			List<TbUser> tbUser = homepageService.getSbUserListByMallIds(mallIds);

			logger.debug("sb users size : " + tbUser.size());

			for (BillingStat stat : statList) {
				stat.mappingMallName(tbUser);
			}
		}

		return;
	}




	/**
	 * 빌링 CDR건수와 채팅 CDR건수가 맞지 않는 데이터 리스트
	 * @return
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> getListMisMatchCdrCount(String fromDate, String toDate) {

		if(fromDate != null && fromDate.isEmpty() == false) {
			fromDate = fromDate.replace(".", "");
		}

		if(toDate != null && toDate.isEmpty() == false) {
			toDate = toDate.replace(".", "");
		}

		List<BillingStat> list = billingStatRepository.findMisMatchCdrCount(fromDate, toDate);

		addMallNameForList(list);

		return list;
	}

	/**
	 * 빌링 CDR건수와 채팅 CDR건수가 맞지 않는 데이터 리스트
	 * @return
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public List<BillingStat> getListMisMatchCdrCount(String ym) {

		List<BillingStat> list = billingStatRepository.findMisMatchCdrCount(ym);

		addMallNameForList(list);

		return list;
	}
}
