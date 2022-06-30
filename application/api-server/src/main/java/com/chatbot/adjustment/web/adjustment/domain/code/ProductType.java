package com.chatbot.adjustment.web.adjustment.domain.code;

public enum ProductType {

	PRODUCT_1("상품 유형 1"),
	PRODUCT_2("상품 유형 2"),;

	private String name;

	private ProductType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return this.toString();
	}
}
