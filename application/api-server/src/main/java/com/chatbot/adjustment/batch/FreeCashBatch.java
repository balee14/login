package com.chatbot.adjustment.batch;

import com.chatbot.adjustment.batch.service.ChatApiService;
import com.chatbot.adjustment.service.MallService;
import com.chatbot.adjustment.web.adjustment.domain.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
public class FreeCashBatch {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MallService mallService;

	@Autowired
	ChatApiService chatApiService;

	/**
	 * 매월초 유료결제건이 있는 몰에 무료건수 300 지급
	 * 정산 처리 건수와 일치하는지 체크
	 * @throws Exception
	 */
	@Scheduled(cron = "0 0 0 1 * *") //매월 1일 0시 0분 0초마다
	public void perform() throws Exception {
		logger.debug("Free Cash Batch Started at : " + new Date());


		List<ServiceInfo> list = mallService.getPaymentMallList();

		for(ServiceInfo serviceInfo : list) {
			//쇼핑몰 상태가 정상일때만 지급 처리
			if(chatApiService.getMallStatus(serviceInfo.getMall_id()) == true) {
				mallService.addFreeCash(serviceInfo.getMall_id(), ServiceInfo.FREE_CASH_AMOUNT, false, false);
			}
		}

		logger.debug("Free Cash Batch Finished");
	}
}
