package com.chatbot.adjustment.web.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthError {

	private String error;
	private String error_description;
}
