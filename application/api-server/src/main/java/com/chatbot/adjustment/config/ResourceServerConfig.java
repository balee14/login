package com.chatbot.adjustment.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private CustomAccessTokenConverter customAccessTokenConverter;

	@Override
	public void configure(final HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.and()
				.authorizeRequests().anyRequest().permitAll();


		// 개발시만 설정
		http.csrf().disable();

		http.logout()
				// 세션초기화
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				// /logout 을 호출할 경우 로그아웃
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				// 로그아웃이 성공했을 경우 이동할 페이지
				.logoutSuccessUrl("/login")
				.permitAll();
	}

	@Override
	public void configure(final ResourceServerSecurityConfigurer config) {
		config.tokenServices(tokenServices());
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setAccessTokenConverter(customAccessTokenConverter);

		/*Resource resource = new ClassPathResource("public.txt");
		String publicKey = null;
		try {
			publicKey = IOUtils.toString(resource.getInputStream(), "UTF-8");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}*/
		//logger.debug(">>> public key : " + publicKey);

		//JWT 알고리즘 HS256 사용
		String secretKey = "TNDtMCEn_TNSTvba7T16E1pK9DxcMa1pMpjJ9IYkmeTHGtcqnkIQ9WmiqeYQeIn6kyfs-UIGiw1hRqdv3BHa5mUqVn0536wDcJFB_niO6evPjt4mi5veMmx4wvGKYvQu3v2_zn0rNz43srWJYWZ7yJrSb_Dsgaw7SIF9fukvV7RQlI-kAdBnyo0y9KMNT4eZJJ39c-cJK9pYzzN7lYSBzLfqdolCiC6qpafIVowJlcQlrhLCtNdIQuSZ6cYBSjDhKTT-R07TG1ZmQRCeyP-7ummKoV1zMeKJGRziAng50U1qjEVSEZFn9d_ThlZj02G-on0VYAxZzs0XzXV8wN7I1g";
		converter.setSigningKey(secretKey);

		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		return defaultTokenServices;
	}



}
