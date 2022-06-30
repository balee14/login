package com.chatbot.login.config;

import com.chatbot.login.login.domain.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class CustomTokenEnhancer implements TokenEnhancer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {


	    logger.debug("principal : " + authentication.getPrincipal().toString());

	    final Map<String, Object> additionalInfo = new HashMap<>();

	    LoginUser loginUser = (LoginUser)authentication.getPrincipal();
	    if(loginUser != null) {
		    additionalInfo.put("userId", loginUser.getUser_id());
		    additionalInfo.put("sub", loginUser.getUsername());
		    additionalInfo.put("fullname", loginUser.getTbUser() != null ? loginUser.getTbUser().getBoss_name() : "");
		    additionalInfo.put("roles", loginUser.getRoles() != null ? loginUser.getRoles().get(0) : "");
		    additionalInfo.put("mallId", loginUser.getMall_id());
		    additionalInfo.put("lat", Calendar.getInstance().getTimeInMillis() / 1000);
	    }

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);

        return accessToken;
    }
}
