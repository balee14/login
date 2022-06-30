package com.chatbot.adjustment.web.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target( { ElementType.FIELD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Component
public @interface Info {
	String value();
}