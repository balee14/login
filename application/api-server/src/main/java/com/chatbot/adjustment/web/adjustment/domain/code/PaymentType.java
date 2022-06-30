package com.chatbot.adjustment.web.adjustment.domain.code;

public enum PaymentType {

	CREDIT("전자결제"),
	BANKBOOK("무통장입금"),
	FREE("무료"),;

	private String name;

	private PaymentType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return this.toString();
	}
}
