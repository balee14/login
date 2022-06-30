package com.chatbot.adjustment.kafka.service;

import com.chatbot.adjustment.kafka.domain.CdrUnit;
import com.chatbot.adjustment.service.MallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CdrUnitsConsumer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MallService mallService;

	private List<CdrUnit> cdrUnits = new ArrayList<>();

	@KafkaListener(topics = "${kafka.consumer.topic}")
	public void onReceiving(CdrUnit cdrUnit, @Header(KafkaHeaders.OFFSET) Integer offset,
	                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
	                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		logger.info("Processing topic = {}, partition = {}, offset = {}, cdrUnit = {}",
				topic, partition, offset, cdrUnit);

		if(cdrUnits.size() == 0) {
			cdrUnits.add(0, cdrUnit);
		} else {
			cdrUnits.set(0, cdrUnit);
		}

		//mall_cash_his insert
		mallService.consumeCash(cdrUnits);


	}
}
