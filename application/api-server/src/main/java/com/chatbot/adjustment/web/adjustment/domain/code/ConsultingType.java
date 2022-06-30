package com.chatbot.adjustment.web.adjustment.domain.code;

public enum ConsultingType {

	TYPE_1("컨설팅 타입 1"),
	TYPE_2("컨설팅 타입 2"),;

	private String name;

	private ConsultingType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return this.toString();
	}
}
