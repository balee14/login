package com.chatbot.adjustment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public OAuth2Authentication extractAuthentication(Map<String, ?> map) {

		logger.debug(">>> extractAuthentication");
		OAuth2Authentication authentication = super.extractAuthentication(map);
		authentication.setDetails(map);

		return authentication;
	}
}
