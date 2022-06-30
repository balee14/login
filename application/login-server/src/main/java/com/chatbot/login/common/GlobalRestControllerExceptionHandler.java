package com.chatbot.login.common;

import com.chatbot.login.common.exception.BaseException;
import com.chatbot.login.common.exception.RestError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@RestController
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalRestControllerExceptionHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(BaseException.class)
	public RestError handle400(HttpServletRequest req, BaseException e) {
		return new RestError(HttpStatus.BAD_REQUEST.value(), req.getRequestURL().toString(), e.getMessage(), e.getClass().getName());
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public RestError handle403(HttpServletRequest req, Exception e) {
		return new RestError(HttpStatus.FORBIDDEN.value(), req.getRequestURL().toString(), e.getMessage(), e.getClass().getName());
	}

	@ExceptionHandler(Throwable.class)
	public RestError defaultHandler(HttpServletRequest req, Throwable e) {
		return new RestError(HttpStatus.INTERNAL_SERVER_ERROR.value(), req.getRequestURL().toString(), e.getMessage(), e.getClass().getName());
	}
}
