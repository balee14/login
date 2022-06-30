package com.chatbot.adjustment.batch;

import com.chatbot.adjustment.batch.service.ChatApiService;
import com.chatbot.adjustment.service.BillingStatService;
import com.chatbot.adjustment.web.common.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Configuration
@EnableScheduling
public class CdrBatch {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String DATE_FORMAT = "yyyyMMDDHH";

	@Autowired
	ChatApiService chatApiService;

	@Autowired
	BillingStatService billingStatService;

	/**
	 * 1시간 간격으로 CDR 개수 조회
	 * 정산 처리 건수와 일치하는지 체크
	 * @throws Exception
	 */
//	@Scheduled(cron = "1 0 * * * *") //매시 1초마다
	public void perform() throws Exception {
		logger.debug("CDR Batch Started at : " + new Date());

		// 배치 시점에서 1시간전의 데이터를 가져온다.
		String statDate = DateUtils.getDateTime(Calendar.HOUR, -1, DATE_FORMAT);
		String from = statDate + "00";
		String to = DateUtils.getCurrentDateTime(DATE_FORMAT) + "00";

		List<Map<Long, Integer>> list = chatApiService.getCdrCount(from, to);

		// 통계 채팅 CDR 카운트 업데이트한다.
		billingStatService.updateChatCdrCount(statDate, list);

		logger.debug("CDR Batch Finished");
	}

}
