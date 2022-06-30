package com.chatbot.login.common.exception;

import lombok.Data;

@Data
public class RestError {

	private int status;
	private String url;
	private String message;
	private String exception;

	public RestError(int status, String url, String message, String exception) {
		this.status = status;
		this.url = url;
		this.message = message;
		this.exception = exception;
	}
}
