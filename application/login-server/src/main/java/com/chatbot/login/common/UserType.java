package com.chatbot.login.common;

import lombok.Getter;

public enum UserType {

	ROLE_HOMEMANAGER("1", "주관리자"),
	ROLE_MANAGER("2", "부관리자"),
	ROLE_USER("3", "상담사"),
	ROLE_ADMIN("4", "내부관리자"),
	ROLE_HOMEPAGEADMIN("5", "홈페이지관리자");

	@Getter
	private String code;

	@Getter
	private String desc;

	UserType(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
