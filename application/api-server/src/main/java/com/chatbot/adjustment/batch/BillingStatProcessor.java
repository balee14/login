package com.chatbot.adjustment.batch;

import com.chatbot.adjustment.batch.domain.Payment;
import org.springframework.batch.item.ItemProcessor;

public class BillingStatProcessor implements ItemProcessor<Payment, Payment> {

	@Override
	public Payment process(Payment item) throws Exception {

		return item;
	}
}
