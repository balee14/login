package com.chatbot.adjustment.web.common;


import com.chatbot.adjustment.web.common.exception.ItemNotFoundException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public String handle403() {
		return "403";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ItemNotFoundException.class)
	public String handle404() {
		return "404";
	}

	@ExceptionHandler(value = Exception.class)
	public String defaultHandler(HttpServletRequest req, Exception e, Model model) throws Exception {

		if(AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}

		model.addAttribute("exception", e);
		model.addAttribute("url", req.getRequestURL());

		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));

		model.addAttribute("errorTrace", errors.toString());

		return "500";
	}
}
