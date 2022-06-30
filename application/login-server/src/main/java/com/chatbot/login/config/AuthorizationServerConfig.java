package com.chatbot.login.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.chatbot.login.service.UserDetailService;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailService userDetailService;

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

	@Bean
	@Primary
	public JdbcClientDetailsService jdbcClientDetailsService(DataSource dataSource) {
		return new JdbcClientDetailsService(dataSource);
	}

	@Autowired
	private ClientDetailsService clientDetailsService;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		//(DB : login.oauth_client_details) 사용
		clients.withClientDetails(clientDetailsService);
		/*
		//재정의 (수동), authorizedGrantTypes에는 5개 타입이 존재: "authorization_code", "password", "client_credentials", "implicit", "refresh_token"
		clients.inMemory()
		.withClient("crient1")
		.secret("secret")
		.authorizedGrantTypes("authorization_code", "password", "refresh_token")
		.scopes("read","write","homepage")
		.accessTokenValiditySeconds(60 * 60 * 24 * 30) //60초*60분*24시간*30일 (default value 60 * 60 * 12)
		.refreshTokenValiditySeconds(60 * 60 * 24 * 30)//60초*60분*24시간*30일 (default value 60 * 60 * 24 * 30)
		.and()
		.withClient("crient2")
		.secret("secret")
		.authorizedGrantTypes("authorization_code", "password", "refresh_token")
		.scopes("read","write","showbot")
		.accessTokenValiditySeconds(60 * 60 * 24 * 30) //60초*60분*24시간*30일 (default value 60 * 60 * 12)
		.refreshTokenValiditySeconds(60 * 60 * 24 * 30)//60초*60분*24시간*30일 (default value 60 * 60 * 24 * 30)
		.and()
		.withClient("crient3")
		.secret("secret")
		.authorizedGrantTypes("authorization_code", "password", "refresh_token")
		.scopes("read","write","billing")
		.accessTokenValiditySeconds(60 * 60 * 24 * 30) //60초*60분*24시간*30일 (default value 60 * 60 * 12)
		.refreshTokenValiditySeconds(60 * 60 * 24 * 30)//60초*60분*24시간*30일 (default value 60 * 60 * 24 * 30)
		;
		*/
	}

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    @Override
    public void configure(final AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
        endpoints.tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancerChain)
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailService);
        	//2021.02.22-grant_type=refresh_token를 사용하기 위해 userDetailsService 로직 추가
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        //final KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("login.jks"), "lgyvuAef".toCharArray());
        //converter.setKeyPair(keyStoreKeyFactory.getKeyPair("login"));
	    //JWT 알고리즘 HS256 사용
	    converter.setSigningKey("TNDtMCEn_TNSTvba7T16E1pK9DxcMa1pMpjJ9IYkmeTHGtcqnkIQ9WmiqeYQeIn6kyfs-UIGiw1hRqdv3BHa5mUqVn0536wDcJFB_niO6evPjt4mi5veMmx4wvGKYvQu3v2_zn0rNz43srWJYWZ7yJrSb_Dsgaw7SIF9fukvV7RQlI-kAdBnyo0y9KMNT4eZJJ39c-cJK9pYzzN7lYSBzLfqdolCiC6qpafIVowJlcQlrhLCtNdIQuSZ6cYBSjDhKTT-R07TG1ZmQRCeyP-7ummKoV1zMeKJGRziAng50U1qjEVSEZFn9d_ThlZj02G-on0VYAxZzs0XzXV8wN7I1g");

        return converter;
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }

}
