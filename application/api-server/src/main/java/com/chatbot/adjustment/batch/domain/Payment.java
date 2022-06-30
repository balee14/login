package com.chatbot.adjustment.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Payment {

	private String date;
	private Long mall_id;
	private long payment_credit;
	private long payment_bankbook;

}
