package com.chatbot.login.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class TokenController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "tokenServices")
    ConsumerTokenServices tokenServices;

    @Resource(name = "tokenStore")
    TokenStore tokenStore;

    @Autowired
    private DefaultTokenServices defaultTokenServices;

	/**
	 * 토큰 삭제 처리
	 * @param request
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/oauth/revoke-token")
	@ResponseStatus(HttpStatus.OK)
	public void revokeToken(HttpServletRequest request) {
		
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			String tokenValue = authHeader.replace("Bearer", "").trim();

			logger.debug(">>> token : " + tokenValue);

			OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);

			logger.debug(">>> accessToken : " + accessToken);
			tokenStore.removeAccessToken(accessToken);

			OAuth2AccessToken accessToken1 = defaultTokenServices.readAccessToken(tokenValue);
			logger.debug(">>> accessToken1 : " + accessToken1);
			defaultTokenServices.revokeToken(tokenValue);
		}
	}

    @RequestMapping(method = RequestMethod.POST, value = "/oauth/token/revokeById/{tokenId}")
    @ResponseBody
    public void revokeToken(HttpServletRequest request, @PathVariable String tokenId) {

		logger.debug(">>> token id : " + tokenId);

        tokenServices.revokeToken(tokenId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/tokens/{clientId}")
    @ResponseBody
    public List<String> getTokens(@PathVariable String clientID) {
        List<String> tokenValues = new ArrayList<String>();
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId(clientID);
        if (tokens != null) {
            for (OAuth2AccessToken token : tokens) {
                tokenValues.add(token.getValue());
            }
        }
        return tokenValues;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/tokens/revokeRefreshToken/{tokenId:.*}")
    @ResponseBody
    public String revokeRefreshToken(@PathVariable String tokenId) {
        if (tokenStore instanceof JdbcTokenStore) {
            ((JdbcTokenStore) tokenStore).removeRefreshToken(tokenId);
        }
        return tokenId;
    }

}