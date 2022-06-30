package com.chatbot.login.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.chatbot.login.common.UserType;
import com.chatbot.login.common.exception.BaseException;
import com.chatbot.login.homepage.domain.TbUser;
import com.chatbot.login.homepage.repository.TbUserRepository;
import com.chatbot.login.login.domain.LoginUser;
import com.chatbot.login.login.domain.User;
import com.chatbot.login.login.repository.UserRepository;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@Service
public class UserDetailService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserDetailService.class);

	@Value("${api.server.host}")
	private String apiServerHost;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TbUserRepository tbUserRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	//@Transactional("transactionManager")
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("[loadUserByUsername] username : {}", username);
		/*try {
			username = new String(Base64.getDecoder().decode(username.getBytes()), "UTF-8");
			logger.info("[loadUserByUsername] username : {}", username);
		} catch (UnsupportedEncodingException e1) {
			logger.error("[loadUserByUsername] UnsupportedEncodingException : " + e1.getMessage());
		}*/
		
		User user = userRepository.findByUsername(username);
		logger.info("[loadUserByUsername] user : {}", user);
		if(user == null)
			throw  new UsernameNotFoundException("[loadUserByUsername] UsernameNotFound [" + username + "]");

		HttpServletRequest request = ((ServletRequestAttributes ) RequestContextHolder.getRequestAttributes()).getRequest();

		if(request != null) {
			//oauth/token?grant_type=password를 통해서 들어온 경우
			String password = Optional.ofNullable(request.getParameter("password")).orElse("");
			if(password.isEmpty()) {
				throw  new UsernameNotFoundException("[loadUserByUsername] PasswordNotFound [" + username + "]");
			}
			boolean isMatched = new BCryptPasswordEncoder().matches(password, user.getPassword());
			//2021.02.22-해당 로직은 열게 되면은 refresh_token 타입도 사용가능하나 isMatched가 true만 넘어가기에 위험성이 있어서 주석처리 합니다.
			/*
			//oauth/token?grant_type=refresh_token을 통해서 들어온 경우
			if(password.isEmpty() && user != null) {
				//해당 유저의 access_token을 재갱신
				password = user.getPassword();
				isMatched = true;
			}
			*/
			//2021.03.09 : 암호화되기 전 비밀번호는 노출X ( 확인하려면 로컬에서 아래 주석을 풀어 확인해 주세요. )
			//logger.info("[loadUserByUsername] password : " + password);
			//2021.02.22-비밀번호는 단반향으로만 암호화가 가능하고 복호화가 가능하지 않습니다. BCryptPasswordEncoder().matches 통해서만 비밀번호 체크가 가능합니다. (복호화X)
			/*try {
				password = new String(Base64.getDecoder().decode(password.getBytes()), "UTF-8");
				logger.info("[loadUserByUsername] password : " + password);
			} catch (UnsupportedEncodingException e1) {
				logger.error("[loadUserByUsername] UnsupportedEncodingException : " + e1.getMessage());
			}*/
		
			logger.info("[loadUserByUsername] password isMatched : " + isMatched);
			if (isMatched == false) {
				logger.error("[loadUserByUsername] passsword incorrected!");

				user.setPassword_failed_count(user.getPassword_failed_count() + 1);

				try {
					if (transactionManager != null) {
						TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
						transactionTemplate.execute(new TransactionCallback<Object>() {

							@Override
							public Object doInTransaction(TransactionStatus status) {
								try {
									userRepository.save(user);
								} catch (Exception e) {
									status.setRollbackOnly();
								}
								return null;
							}
						});
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				// 패스워드 실패 연속 5회이상시 패스워드 초기화
				if(user.getPassword_failed_count() >= 5) {
					logger.error("[loadUserByUsername] passsword count initailize!");

					TbUser tbUser = tbUserRepository.findBySsoUserId(user.getUser_id());

					if(tbUser != null) {

						//패스워드 초기화 호출
						final Map<String, String> params = new HashMap<String, String>();
						params.put("user_name", user.getUsername());
						params.put("email", tbUser.getUser_email());

						final Response response = RestAssured.given()
								.with()
								.params(params)
								.when()
								.post(apiServerHost + "/api/pw_reset");
					}
				}

				throw new BaseException(String.valueOf(user.getPassword_failed_count()));
			}
		}

		logger.info("[loadUserByUsername] password_failed_count : {}", user.getPassword_failed_count());
		// 로그인 성공시 패스워드 실패 카운트 초기화
		if(user.getPassword_failed_count() > 0) {
			user.setPassword_failed_count(0);
			userRepository.save(user);
		}

		LoginUser loginUser = createUser(user);
		logger.info("[loadUserByUsername] loginUser : {}", loginUser);
		
		return loginUser;
	}

	@Transactional("homepageTransactionManager")
	private LoginUser createUser(User user) {

		LoginUser loginUser = new LoginUser(user);
		loginUser.setRoles(Arrays.asList(getUserType(loginUser.getUser_type())));
		
		// user_type == 1이면 잘못된 user_id를 쿼리에 전달하여 상단에 이름이 나오지 않아 수정
		// 시스템 관리자 일 때 이름이 '시스템'으로 고정되는 현상 수정
//		loginUser.setPbUsers(pbUsersRepository.findBySsoUserId(user.getUser_id()));
//		loginUser.setTbUser(tbUserRepository.findBySsoUserId(user.getMall_id()));
		loginUser.setTbUser(tbUserRepository.findByUsername(user.getUsername()));
		
		return loginUser;
	}

	private String getUserType(String type) {

		for(UserType userType : UserType.values()) {
			if(userType.getCode().equals(type)) {
				return userType.name();
			}
		}

		return null;
	}
}
