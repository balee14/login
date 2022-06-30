package com.chatbot.adjustment.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@Order(0)
@ComponentScan("com.chatbot.adjustment.web.controller")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/fonts/**", "/png/**");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//http.sessionManagement()
		//		.maximumSessions(2);

			/*.authorizeRequests()
				.antMatchers("/login", "/login-error").permitAll()
				.antMatchers("/user/add", "/hello").permitAll()
				.antMatchers("/auth/user/pw_reset/**").permitAll()
				.anyRequest().authenticated();*/

		/*http.authorizeRequests()
				.antMatchers("/auth/**").authenticated()
				//.antMatchers("/auth/**").hasAnyRole("USER_MASTER", "USER_MEMBER", "USER_CONSULTANT", "ADMIN")
				//.antMatchers("/auth/hello").hasRole("ADMIN")
				.antMatchers("/admin/**").hasAnyRole("USER_MASTER", "USER_MEMBER", "USER_CONSULTANT", "ADMIN")
				.antMatchers("/login", "/login-error").permitAll()
				.antMatchers("/user/add", "/hello").permitAll();
				//.anyRequest().authenticated();*/


		/*http.formLogin()
				// 로그인 페이지
				.loginPage("/login")
				// 로그인 처리 페이지
				.loginProcessingUrl("/loginProcessing")
				.failureUrl("/login-error")
				.permitAll();

		http.rememberMe()
				.key(REMEMBER_ME_KEY)
				.rememberMeServices(tokenBasedRememberMeServices());

		http.logout()
				// 세션초기화
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				// /logout 을 호출할 경우 로그아웃
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				// 로그아웃이 성공했을 경우 이동할 페이지
				.logoutSuccessUrl("/login")
				.permitAll();*/


		// 개발시만 설정
		//http.csrf().disable();
	}

}
