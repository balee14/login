package com.chatbot.adjustment.service;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.exception.AuthError;
import com.chatbot.adjustment.web.common.exception.BaseException;
import com.chatbot.adjustment.web.common.vo.CommonVO;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.repository.TbUserRepository;
import com.chatbot.adjustment.web.login.domain.User;
import com.chatbot.adjustment.web.login.domain.code.UserType;
import com.chatbot.adjustment.web.login.repository.UserRepository;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Service
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Value("${login.oauth-token.uri:http://localhost:8080/oauth/token}")
	private String oauthTokenUri;

	@Value("${login.oauth-token.client-id:client1}")
	private String clientID;

	@Value("${login.oauth-token.client-secret:secret}")
	private String clientSecret;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TbUserRepository tbUserRepository;
	
	@Autowired
	private MailService mailService;

	@Autowired
	private HomepageService homepageService;

	@Autowired
	private ResultCodeComponent codeComponent;
	
	@Transactional(value = "transactionManager")
	public User get(User user) {
		return getByUserName(user.getUsername());
	}

	@Transactional(value = "transactionManager")
	public User getByUserName(String userName) {

		return userRepository.findByUsername(userName);
	}
	
	
	/*
	// ID ???????????? API ??????
	// ???????????? ????????? ??????????????? ?????? ??????
	@SuppressWarnings("unchecked")
	public Map<String, Object> idDuplChk(String user_login) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
//		String param = "adverId=" + userId;
//		
//		HttpPost httpPost = new HttpPost(idDuplChkUrl + "?" + param);
//		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
//		
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + idDuplChkUrl + "?" + param);
		
//		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
//			String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//			System.out.println("Response : " + content);
//			
//			JSONParser jparser = new JSONParser();
//			JSONObject resultJObj = (JSONObject) jparser.parse(content);
//			JSONObject dataJObj = (JSONObject) jparser.parse(resultJObj.getAsString("data"));
//			
//			if ("true".equals(dataJObj.getAsString("flag"))) {
//				map.put("success", true);
//				map.put("error", resultJObj.getAsString("error"));
//				map.put("accessTime", resultJObj.getAsString("startTime"));
//			} else {
//				map.put("success", false);
//				map.put("error", resultJObj.getAsString("error"));
//				map.put("accessTime", resultJObj.getAsString("startTime"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		Date currentTime = Calendar.getInstance().getTime();
		long epochTime = currentTime.getTime();
		
		try {
			// ???????????? : ??????????????? ?????? ?????? homepage.tb_user??? login.user ???????????? ????????? ??????
			// homepage.tb_user ???????????? ??????
			TbUser tbUser = tbUserRepository.findByUserLogin(user_login);
			// login.user ???????????? ?????? 
			User user = userRepository.findByUsername(user_login);
			
			if (tbUser == null && user == null) {
				logger.info("[idDuplChk] code : USER_0000_CODE");
				logger.info("[idDuplChk] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
			} else {
				logger.error("[idDuplChk] code : USER_1000_CODE");
				logger.error("[idDuplChk] msg : {}", codeComponent.getCodeMsg("USER_1000_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_1000_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[idDuplChk] code : USER_9999_CODE");
			logger.error("[idDuplChk] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("accessTime", epochTime);
		}
		
		return map;
	}
	*/
	
	/**
	 * tb_user ???????????? ??????
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public TbUser selectTbUserInfo(String userId) throws Exception {

		logger.info("[selectTbUserInfo] userId : {}", userId);
		
		// tb_user ???????????? ??????
		TbUser tbUser = tbUserRepository.findByUserLogin(userId);
		
		return tbUser;
	}
	
	// tb_user ???????????? ??????
	/*
	public TbUser selectTbUserInfoWithPass(String userId, String userPass) throws Exception {

		logger.info("[selectTbUserInfoWithPass] userId : {}", userId);
		logger.info("[selectTbUserInfoWithPass] userPass : {}", userPass);
		
		TbUser returnTbUser = null;
		
		// tb_user ???????????? ??????
		TbUser tbUser = tbUserRepository.findByUserLogin(userId);
		logger.info("[selectTbUserInfoWithPass] tbUser : {}", tbUser);

		if( tbUser != null ) {
			boolean isUser = new BCryptPasswordEncoder().matches(userPass, tbUser.getUser_pass());
			if (isUser) {
				return tbUser;
			}
		}
		
		return returnTbUser;
	}
	*/
	
	/**
	 * ???????????? API ??????
	 * ???????????? ????????? ??????????????? ?????? ????????? ???????????? ??????
	 * @param reqParam
	 * @param userCreateUrl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> userCreate(CommonVO reqParam, String userCreateUrl) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		String param = "adverId=" + reqParam.getUserId();
		param += "&upasswd=" + URLEncoder.encode(reqParam.getUserPass(), "UTF-8");
		param += "&corpName=" + URLEncoder.encode(reqParam.getCompanyName(), "UTF-8");
		param += "&bnumber=" + reqParam.getCompanyNo();
		param += "&email=" + reqParam.getEmail();
		
		HttpPost httpPost = new HttpPost(userCreateUrl + "?" + param);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + userCreateUrl + "?" + param);
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {

			String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			logger.info("[userCreate] httpPost response : {}", content);
			
			JSONParser jparser = new JSONParser();
			JSONObject resultJObj = (JSONObject) jparser.parse(content);
			
			if ("true".equals(resultJObj.get("success").toString())) {
				map.put("success", true);
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
			} else {
				map.put("success", false);
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/*
	// ???????????? ?????? ??????
	// ???????????? ????????? ??????????????? ?????? ?????? ?????? ??????
	@Transactional("homepageTransactionManager")
	public int registerUser(CommonVO reqParam) throws Exception {
		
		int resultCode = 1;
		
		try {
			// login.user ???????????? ?????? ??????
			User addUser = new User();
			addUser.setPassword(new BCryptPasswordEncoder().encode(reqParam.getUserPass()));
			addUser.setUsername(reqParam.getUserId());
			addUser.setUser_type(reqParam.getUserType());
			addUser.setEnabled( false );
			addUser.setPassword_failed_count(0);
			if( addUser.getUser_type().equals(UserType.ROLE_HOMEMANAGER.getCode()) ) {
				addUser.setMall_id(0);
			}
			userRepository.save(addUser);
			
			// mall_id ????????????
			if ( addUser.getUser_type().equals(UserType.ROLE_ADMIN.getCode()) ) {
				addUser.setMall_id(addUser.getUser_id());
				addUser.setEnabled( true );
				userRepository.save(addUser);
			} else if( addUser.getUser_type().equals(UserType.ROLE_HOMEMANAGER.getCode()) ) {
				addUser.setMall_id(0);
				addUser.setEnabled( true );
				userRepository.save(addUser);
			}
			
			// homepage.tb_user ???????????? ?????? ??????
			TbUser tbUser = new TbUser();
			tbUser.setUser_login(reqParam.getUserId());
			tbUser.setUser_email(Optional.ofNullable(reqParam.getEmail()).orElse(""));
			tbUser.setUser_status(1);
			tbUser.setAgreement001(Optional.ofNullable(reqParam.getAgreement001()).orElse("N"));
			tbUser.setAgreement002(Optional.ofNullable(reqParam.getAgreement002()).orElse("N"));
			tbUser.setAgreement003(Optional.ofNullable(reqParam.getAgreement003()).orElse("N"));
			tbUser.setAgreement004(Optional.ofNullable(reqParam.getAgreement004()).orElse("N"));
				
			if ( addUser.getUser_type().equals(UserType.ROLE_ADMIN.getCode()) ) {
				tbUser.setSso_user_id(addUser.getUser_id()); //????????? ??? ???????????????(?????????-login??????????????? auto??? ????????? ???)
				tbUser.setSso_mall_id(addUser.getUser_id()); //????????? ??? ???????????????(?????????-login??????????????? auto??? ????????? ???)
				tbUser.setSso_user_role(4);
			} else if( addUser.getUser_type().equals(UserType.ROLE_HOMEMANAGER.getCode()) ) {
				tbUser.setSso_user_id(0);
				tbUser.setSso_mall_id(0);
				tbUser.setSso_user_role(1);
			}
				
			tbUser.setLogin_failed_cnt(0);															//????????? ?????? ??????
			tbUser.setBiz_name(Optional.ofNullable(reqParam.getCompanyName()).orElse(""));			//?????????
			tbUser.setBiz_no(Optional.ofNullable(reqParam.getCompanyNo()).orElse(""));				//???????????????
			tbUser.setBoss_name(Optional.ofNullable(reqParam.getCompanyName()).orElse(""));         //????????????(?????????)
			tbUser.setUser_pass(addUser.getPassword());												//???????????? ????????????
			tbUser.setUser_name(Optional.ofNullable(reqParam.getUserName()).orElse("")); 		 	//??????(????????????)
			tbUser.setBiz_address(Optional.ofNullable(reqParam.getCompanyAddress()).orElse("||")); 	//??????(??????) : ???????????? = ????????????|????????????|????????????
			tbUser.setUser_phone(Optional.ofNullable(reqParam.getUserTel()).orElse("")); 		 	//??????(?????????)
			String recommandId = Optional.ofNullable(reqParam.getRecommendId()).orElse("0");		//??????(??????????????????) : ????????????0 (???????????? ???????????????)
			if(recommandId.trim().isEmpty()) {
				recommandId = "0";
			}
			tbUser.setRecommend_id(Long.parseLong(recommandId)); 
			tbUserRepository.save(tbUser);
		}
		catch (Exception e) {
			e.printStackTrace();
			resultCode = 0;
		}
		
		return resultCode;
	}
	*/
	
	/**
	 * ???????????? ??????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public int selectIsUser(CommonVO reqParam) throws Exception {
		
		int cnt = 0;
		TbUser tbUserParam = new TbUser();
		tbUserParam.setUser_login(reqParam.getUserId());
		tbUserParam.setAccess_token(reqParam.getToken());
		
		// ???????????? ??????
		TbUser tbUser = tbUserRepository.findByUserLoginToken(tbUserParam.getUser_login(), tbUserParam.getAccess_token());
		
		if (tbUser != null && ! StringUtils.isEmpty(tbUser.getUser_login())) {
			cnt = 1;
		} else {
			cnt = 0;
		}
		
		return cnt;
	}
	
	/**
	 * ???????????? API ??????
	 * @param reqParam
	 * @param userupdateUrl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> userUpdate(CommonVO reqParam, String userupdateUrl) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		String upasswd = "";
		String uname = "";
		String hp = "";
		String corpName = "";
		String bnumber = "";
		String baddress = "";
		String email = "";
		
		if (! StringUtils.isEmpty(reqParam.getNewUserPass())) {
			upasswd = reqParam.getNewUserPass();
		}
		if (! StringUtils.isEmpty(reqParam.getUserName())) {
			uname = reqParam.getUserName();
		}
		if (! StringUtils.isEmpty(reqParam.getUserTel())) {
			hp = reqParam.getUserTel();
		}
		if (! StringUtils.isEmpty(reqParam.getCompanyName())) {
			corpName = reqParam.getCompanyName();
		}
		if (! StringUtils.isEmpty(reqParam.getCompanyNo())) {
			bnumber = reqParam.getCompanyNo();
		}
		if (! StringUtils.isEmpty(reqParam.getCompanyAddress())) {
			baddress = reqParam.getCompanyAddress();
		}
		if (! StringUtils.isEmpty(reqParam.getEmail())) {
			email = reqParam.getEmail();
		}
		
		String param = "adverId=" + reqParam.getUserId() + "&upasswd=" + URLEncoder.encode(upasswd, "UTF-8")
						+ "&uname=" + uname + "&hp=" + hp
						+ "&corpName=" + URLEncoder.encode(corpName, "UTF-8") + "&bnumber=" + bnumber
						+ "&baddress=" + URLEncoder.encode(baddress, "UTF-8") + "&email=" + email;
		
		HttpPost httpPost = new HttpPost(userupdateUrl + "?" + param);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + userupdateUrl + "?" + param);
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {

			String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			logger.info("[userUpdate] httpPost response : {}", content);
			
			JSONParser jparser = new JSONParser();
			JSONObject resultJObj = (JSONObject) jparser.parse(content);

			if ("true".equals(resultJObj.getAsString("success"))) {
				map.put("success", true);
			} else {
				map.put("success", false);
			}
			
			map.put("error", resultJObj.getAsString("error"));
			map.put("accessTime", resultJObj.getAsString("startTime"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ???????????? ?????? API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@Transactional("homepageTransactionManager")
	public int updateUser(CommonVO reqParam) throws Exception {
		
		String newUserPass = "";
		
		// ???????????? ??????
		TbUser tbUser = tbUserRepository.findByUserLogin(reqParam.getUserId());

		TbUser saveTbUser = new TbUser();
		saveTbUser.setUser_login(reqParam.getUserId());
		if (! StringUtils.isEmpty(reqParam.getUserPass()) && ! StringUtils.isEmpty(reqParam.getNewUserPass())) {
			newUserPass = new BCryptPasswordEncoder().encode(reqParam.getNewUserPass());
			saveTbUser.setUser_pass(newUserPass);
		} else {
			saveTbUser.setUser_pass(tbUser.getUser_pass());
		}
		if (! StringUtils.isEmpty(reqParam.getCompanyName())) {
			saveTbUser.setBiz_name(reqParam.getCompanyName());
		} else {
			saveTbUser.setBiz_name(tbUser.getBiz_name());
		}
		/*
		if (! StringUtils.isEmpty(reqParam.getCompanyNo())) {
			saveTbUser.setBiz_no(reqParam.getCompanyNo());
		} else {
			saveTbUser.setBiz_no(tbUser.getBiz_no());
		}
		*/
		saveTbUser.setBiz_no(reqParam.getCompanyNo()); //?????? ?????? 10??????????????? ??????(??????:111-11-11111)
		if (! StringUtils.isEmpty(reqParam.getCompanyAddress())) {
			saveTbUser.setBiz_address(reqParam.getCompanyAddress());
		} else {
			saveTbUser.setBiz_address(tbUser.getBiz_address());
		}
		if (! StringUtils.isEmpty(reqParam.getUserName())) {
			saveTbUser.setUser_name(reqParam.getUserName());
		} else {
			saveTbUser.setUser_name(tbUser.getUser_name());
		}
		if (! StringUtils.isEmpty(reqParam.getEmail())) {
			saveTbUser.setUser_email(reqParam.getEmail());
		} else {
			saveTbUser.setUser_email(tbUser.getUser_email());
		}
		if (! StringUtils.isEmpty(reqParam.getUserTel())) {
			saveTbUser.setUser_phone(reqParam.getUserTel());
		} else {
			saveTbUser.setUser_phone(tbUser.getUser_phone());
		}
		if (! StringUtils.isEmpty(reqParam.getAgreement003())) {
			saveTbUser.setAgreement003(reqParam.getAgreement003());
		} else {
			saveTbUser.setAgreement003(tbUser.getAgreement003());
		}
		
		// ???????????? ??????
		int cnt = tbUserRepository.updateTbUserInfo(saveTbUser.getUser_login(), saveTbUser.getUser_pass(), saveTbUser.getBiz_name()
													, saveTbUser.getBiz_no(), saveTbUser.getBiz_address(), saveTbUser.getUser_name()
													, saveTbUser.getUser_email(), saveTbUser.getUser_phone(), saveTbUser.getAgreement003());
		
		if (cnt > 0 && ! StringUtils.isEmpty(reqParam.getUserPass()) && ! StringUtils.isEmpty(reqParam.getNewUserPass())) {
			
			// login.user ???????????? ??????
			User user = userRepository.findByUsername(reqParam.getUserId());
			
			User saveUser = new User();
			saveUser.setUsername(reqParam.getUserId());
			saveUser.setPassword_failed_count(user.getPassword_failed_count());
			if (reqParam.getUserPass() != null && reqParam.getNewUserPass() != null) {
				saveUser.setPassword(newUserPass);
			} else {
				saveUser.setPassword(user.getPassword());
			}
			
			// login.user ???????????? ????????????
			cnt = userRepository.updatePassword(saveUser.getUsername(), saveUser.getPassword(), saveUser.getPassword_failed_count());
		}

		return cnt;
	}
	
	/**
	 * ???????????? ?????? API
	 * ???????????? ????????? ??????????????? ?????? ????????? ???????????? ??????
	 * @param userId
	 * @param userDetailUrl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getUserInfo(String userId, String userDetailUrl) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		String param = "adverId=" + userId;
		
		HttpPost httpPost = new HttpPost(userDetailUrl + "?" + param);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + userDetailUrl + "?" + param);
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {

			String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			logger.info("[getUserInfo] httpPost response : {}", content);
			
			JSONParser jparser = new JSONParser();
			JSONObject resultJObj = (JSONObject) jparser.parse(content);
			JSONObject dataJObj = null;
			
			if (! StringUtils.isEmpty(resultJObj.getAsString("data"))) {
				dataJObj = (JSONObject) jparser.parse(resultJObj.getAsString("data"));
			}
			
			if (dataJObj != null) {
				JSONObject returnDateJObj = new JSONObject();
				returnDateJObj.put("userId", dataJObj.getAsString("adverId"));
				returnDateJObj.put("companyName", dataJObj.getAsString("corpName"));
				returnDateJObj.put("companyNo", dataJObj.getAsString("bnumber"));
				returnDateJObj.put("companyAddress", dataJObj.getAsString("baddress"));
				returnDateJObj.put("userName", dataJObj.getAsString("uname"));
				returnDateJObj.put("email", dataJObj.getAsString("email"));
				returnDateJObj.put("userTel", dataJObj.getAsString("hp"));
				
				map.put("data", returnDateJObj);
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
				
				if ("true".equals(resultJObj.getAsString("success"))) {
					map.put("success", true);
				} else {
					map.put("success", false);
				}
			} else {
				map.put("success", resultJObj.getAsString("success"));
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
				map.put("data", resultJObj.getAsString("data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ????????? ???????????? ??????(?????????)
	 * ???????????? ????????? ??????????????? ?????? ????????? ???????????? ??????
	 * @param reqParam
	 * @param userLoginUrl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> userLogin(CommonVO reqParam, String userLoginUrl) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		String param = "adverId=" + reqParam.getUserId();
		param += "&upasswd=" + URLEncoder.encode(reqParam.getUserPass(), "UTF-8");
		
		HttpPost httpPost = new HttpPost(userLoginUrl + "?" + param);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + userLoginUrl + "?" + param);
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
			String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			logger.info("[userLogin] httpPost response : {}", content);
			
			JSONParser jparser = new JSONParser();
			JSONObject resultJObj = (JSONObject) jparser.parse(content);
			JSONObject dataJObj = (JSONObject) jparser.parse(resultJObj.getAsString("data"));
			
			if ("true".equals(dataJObj.getAsString("flag"))) {
				map.put("success", true);
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
			} else {
				map.put("success", false);
				map.put("error", resultJObj.getAsString("error"));
				map.put("accessTime", resultJObj.getAsString("startTime"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ?????? ??????
	 * @param userId
	 * @param userPass
	 * @param createTokenUrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String createToken(String userId, String userPass, String createTokenUrl) throws UnsupportedEncodingException {
		
		int statusCode = 0;
		String token = null;
		String content = "";
		String authKey = "client1:secret";
		
		authKey = Base64.getEncoder().encodeToString(authKey.getBytes());
		//userId = Base64.getEncoder().encodeToString(userId.getBytes());
		//userPass = Base64.getEncoder().encodeToString(userPass.getBytes());
		//logger.info("[createToken] Base64 Encoder username : {}", userId);
		//logger.info("[createToken] Base64 Encoder password : {}", userPass);
		logger.info("[createToken] Base64 Encoder authKey : {}", authKey);
		
		/*String param = "username=" + userId;
		param += "&password=" + userPass;
		param += "&grant_type=password";*/
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("username", userId));
		param.add(new BasicNameValuePair("password", userPass));
		param.add(new BasicNameValuePair("grant_type", "password"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param);
		
		HttpPost httpPost = new HttpPost(createTokenUrl);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + authKey);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
		httpPost.setEntity(entity);
    	
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		logger.info("[createToken] Call login server : {}", createTokenUrl + "?" + param);
		
    	try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
    		content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
    		statusCode = httpResponse.getStatusLine().getStatusCode();
    		logger.info("[createToken] statusCode : {}, httpPost response : {}", statusCode, content);
    		
 	        if (statusCode >= 200 && statusCode < 300) {
 	        	JSONObject result = (JSONObject)new JSONParser().parse(content);
 	        	token = result.getAsString("access_token");
 	        	
 	        	// tb_user??? ?????? ??????
 	        	Long dateLongTmp = Long.parseLong(result.getAsString("lat")) + Long.parseLong(result.getAsString("expires_in"));
 	        	Date date = new Date((dateLongTmp * 1000));
 	        	
 	        	// ????????? localtime
 	            String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
 	           logger.info("[createToken] [ " + nowTime + "] [ "+  Thread.currentThread().getName() + "] Update Before Query user id  : " + result.getAsString("sub").toString() + ", token : " + token );
 	        	
 	        	//! ??? ?????? ???????????? ?????? ??????.
 	        	tbUserRepository.updateTbUserToken(token, date, result.getAsString("sub").toString());
 	        	
 	        	logger.info("[createToken] [ " + nowTime + "] [ "+  Thread.currentThread().getName() + "] Update After Query user id  : " + result.getAsString("sub").toString() + ", token : " + token);
 	        }
 	        
    	} catch (IOException e) {    		
    		e.printStackTrace();
    		logger.error("[createToken] IOException : {}", e.getMessage());
	    } catch (ParseException e) {
	    	e.printStackTrace();
	    	logger.error("[createToken] ParseException : {}", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[createToken] Exception : {}", e.getMessage());
		}
    	
		return token;
	}

	/**
	 * ?????? ??????
	 * @param commonVo
	 * @param deleteTokenUrl
	 * @return
	 */
	public Map<String, Object> deleteToken(CommonVO commonVo, String deleteTokenUrl) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		HttpGet httpGet = new HttpGet(deleteTokenUrl);
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + commonVo.getToken());
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		System.out.println("Call Mobon API : " + deleteTokenUrl + "?" + "header=" + commonVo.getToken());
		
		try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
			// tb_user ??????????????? ?????? ??????
//			int cnt = tbUserRepository.updateTbUserToken("", null, commonVo.getUserId());

			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			map.put("accessTime", epochTime);
			
//			if (cnt > 0) {
				map.put("success", true);
				map.put("error", null);
//			} else {
//				map.put("success", false);
//				map.put("error", "updateTbUserToken fail");
//			}
			map.put("data", null);	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	/**
	 * ????????? ??????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> findUserId(CommonVO reqParam) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// ID ??????
			List<TbUser> tbUserInfo = tbUserRepository.findByBizNameAndBizNoAndUserEmail(reqParam.getCompanyName(), reqParam.getCompanyNo(), reqParam.getEmail());
			
			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			
			if (tbUserInfo != null && tbUserInfo.size() > 0) {
				logger.info("[findUserId] code : USER_0000_CODE");
				logger.info("[findUserId] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
			} else {
				logger.error("[findUserId] code : USER_0006_CODE");
				logger.error("[findUserId] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0006_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ????????? ???????????? ????????????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateTbUserAuthNumber(CommonVO reqParam) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// ???????????? ????????? ?????????????????? ????????????
			int successCnt = 0;
			if (reqParam.getUserId() != null) {
				// ???????????? ????????????
				successCnt = tbUserRepository.updateTbUserAuthNumberById(reqParam.getAuthNumber(), reqParam.getUserId(), reqParam.getEmail());
			} else {
				// ???????????? ????????????
				successCnt = tbUserRepository.updateTbUserAuthNumber(reqParam.getAuthNumber(), reqParam.getCompanyName(), reqParam.getCompanyNo(), reqParam.getEmail());
			}
			
			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			
			if (successCnt > 0) {
				logger.info("[updateTbUserAuthNumber] code : USER_0000_CODE");
				logger.info("[updateTbUserAuthNumber] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.replace("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
			} else {
				logger.error("[updateTbUserAuthNumber] code : USER_0004_CODE");
				logger.error("[updateTbUserAuthNumber] msg : {}", codeComponent.getCodeMsg("USER_0004_CODE"));
				map.put("success", false);
				map.replace("error", codeComponent.getCodeMsg("USER_0004_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ???????????? ??????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> findUserPw(CommonVO reqParam) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// ???????????? ??????
			List<TbUser> tbUserInfo = tbUserRepository.findByUserLoginAndUserEmail(reqParam.getUserId(), reqParam.getEmail());
			
			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			
			if (tbUserInfo != null && tbUserInfo.size() > 0) {
				logger.info("[findUserPw] code : USER_0000_CODE");
				logger.info("[findUserPw] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
			} else {
				logger.error("[findUserPw] code : USER_0006_CODE");
				logger.error("[findUserPw] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0006_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ??????????????? ?????????????????? ???????????? ??????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> checkAuthNum(CommonVO reqParam) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// ???????????? ??????
			List<TbUser> tbUserInfo = null;
			if (StringUtils.isEmpty(reqParam.getUserId())) {
				tbUserInfo = tbUserRepository.findByBizNameAndBizNoAndUserEmailAndAuthNumber(reqParam.getCompanyName(), reqParam.getCompanyNo(), reqParam.getEmail(), reqParam.getAuthNumber());
			} else {
				tbUserInfo = tbUserRepository.findByUserLoginAndUserEmailAndAuthNumber(reqParam.getUserId(), reqParam.getEmail(), reqParam.getAuthNumber());
			}
			
			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			
			if (tbUserInfo != null && tbUserInfo.size() > 0) {
				logger.info("[checkAuthNum] code : USER_0000_CODE");
				logger.info("[checkAuthNum] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.put("id", tbUserInfo.get(0).getUser_login());
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
			} else {
				logger.error("[checkAuthNum] code : USER_0007_CODE");
				logger.error("[checkAuthNum] msg : {}", codeComponent.getCodeMsg("USER_0007_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0007_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ?????????????????? ???????????? ?????????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> changeUserPw(CommonVO reqParam) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// ?????????????????? ???????????? ?????????
			String newUserPass = new BCryptPasswordEncoder().encode(reqParam.getUserPass());
			int resultCnt = tbUserRepository.updateTbUserPasswordByAuthNumber(reqParam.getUserId(), newUserPass, reqParam.getAuthNumber());
			
			Date currentTime = Calendar.getInstance().getTime();
			long epochTime = currentTime.getTime();
			
			if (resultCnt > 0) {
				// login.user ???????????? ????????????
				int cnt = userRepository.updatePassword(reqParam.getUserId(), newUserPass, 0);
				
				if (cnt > 0) {
					logger.info("[changeUserPw] code : USER_0000_CODE");
					logger.info("[changeUserPw] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
					map.put("success", true);
					map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
					map.put("accessTime", epochTime);
				} else {
					logger.error("[changeUserPw] code : USER_6000_CODE");
					logger.error("[changeUserPw] msg : {}", codeComponent.getCodeMsg("USER_6000_CODE"));
					map.put("success", false);
					map.put("error", codeComponent.getCodeMsg("USER_6000_CODE"));
					map.put("accessTime", epochTime);
				}
			} else {
				logger.error("[changeUserPw] code : USER_6000_CODE");
				logger.error("[changeUserPw] msg : {}", codeComponent.getCodeMsg("USER_6000_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_6000_CODE"));
				map.put("accessTime", epochTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	/**
	 * ????????? ??????
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@Transactional(value = "transactionManager")
	public User add(User user) throws Exception {

		if(user.getUsername() == null || user.getUsername().isEmpty())
			throw new BaseException("????????? ????????? ????????????.");

		if(user.getPassword() == null || user.getPassword().isEmpty())
			throw new BaseException("????????? ??????????????? ????????????.");

		if(user.getUser_type() == null || user.getUser_type().isEmpty())
			throw new BaseException("????????? ????????? ????????????.");

		if(userRepository.findByUsername(user.getUsername()) != null)
			throw new BaseException("?????? ????????? ????????? ???????????????.");

		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

		User addUser = new User();
		addUser.setUsername(user.getUsername());
		addUser.setPassword(user.getPassword());
		addUser.setUser_type(user.getUser_type());

		//???????????? ?????? ??????
//		if(user.getMall_id() > 0) {
//			addUser.setMall_id(user.getMall_id());
//		}

		userRepository.save(addUser);

		// ?????????????????? user_id??? mall_id??? ??????
//		if(user.getUser_type().equals(UserType.ROLE_HOMEMANAGER.getCode())) {
//			addUser.setMall_id(addUser.getUser_id());
//			userRepository.save(addUser);
//
//			mallService.addServiceInfo(addUser.getMall_id());
//
//			//?????? ???????????? ???????????? ?????? ?????? ?????? API ?????? mall id ????????????
//			chatApiService.changeBalanceStatus(addUser.getMall_id(), false);
//		}

		// user_id??? mall_id??? ????????? ?????? ?????? ????????? ?????? ??????
		// ???????????????(user_type == 4)?????? mall_id??? user_id??? ????????? ??????
		if (user.getUser_type().equals(UserType.ROLE_ADMIN.getCode())) {
			// mall_id ????????????
			addUser.setMall_id(addUser.getUser_id());
			userRepository.save(addUser);
		}
		
		return addUser;
	}

	/**
	 * ???????????? ?????????
	 * @param userName
	 * @param userEmail
	 */
	@Transactional("transactionManager")
	public void resetPassword(String userName, String userEmail) {

		if(userName == null || userName.isEmpty())
			throw new BaseException("????????? ???????????? ????????????.");

		if(userEmail == null || userEmail.isEmpty())
			throw new BaseException("????????? ???????????? ????????????.");

		User user = userRepository.findByUsername(userName);
		if(user == null)
			throw new BaseException("???????????? ?????? ??? ????????????.");

		resetPassword(user, userEmail);
	}

	/**
	 * ???????????? ?????????
	 * @param userID
	 * @param userEmail
	 */
	@Transactional("transactionManager")
	public void resetPassword(long userID, String userEmail) {

		if(userID == 0)
			throw new BaseException("????????? ???????????? ????????????.");

		if(userEmail == null || userEmail.isEmpty())
			throw new BaseException("????????? ???????????? ????????????.");

		User user = userRepository.findOne(userID);
		if(user == null)
			throw new BaseException("???????????? ?????? ??? ????????????.");

		resetPassword(user, userEmail);
	}

	private void resetPassword(User user, String userEmail) {
		// get email
		String email = homepageService.getEmail(user.getUser_id());

		if(userEmail.equals(email) == false)
			throw new BaseException("????????? ????????? ????????????.");

		// reset password
		String password = generatePassword();
		user.setPassword(new BCryptPasswordEncoder().encode(password));

		userRepository.save(user);

		// send mail
		mailService.sendMessageUsingTemplate(email, "[??????] ???????????? ?????? ??????????????? ?????? ????????????.", password, "");

		logger.debug("[resetPassword] user password : {}", password);
	}

	/**
	 * @param userName
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 */
	@Transactional("transactionManager")
	public AuthError passwordChange(String userName, String oldPassword, String newPassword) {

		if(userName == null || userName.isEmpty())
			throw new BaseException("????????? ???????????? ????????????.");

		if(oldPassword == null || oldPassword.isEmpty())
			throw new BaseException("?????? ??????????????? ????????????.");

		if(newPassword == null || newPassword.isEmpty())
			throw new BaseException("?????? ??????????????? ????????????.");

		//???????????????????????? ??????
		checkAuthrize(userName);

		User user = userRepository.findByUsername(userName);
		if(user == null)
			throw new BaseException("???????????? ?????? ??? ????????????.");

		boolean isMatched = new BCryptPasswordEncoder().matches(oldPassword, user.getPassword());
		if (isMatched == false) {
			logger.debug("[passwordChange] passsword incorrected!");

			user.setPassword_failed_count(user.getPassword_failed_count() + 1);
			userRepository.save(user);

			// ???????????? ?????? ?????? 5???????????? ???????????? ?????????
			if(user.getPassword_failed_count() >= 5) {
				logger.debug("[passwordChange] passsword count initailize!");
				String email = homepageService.getEmail(user.getUser_id());
				resetPassword(user.getUser_id(), email);
			}

			AuthError authError = new AuthError("unauthorized", String.valueOf(user.getPassword_failed_count()));

			return authError;
		}

		// ???????????? ?????? ????????? ?????????
		if(user.getPassword_failed_count() > 0) {
			user.setPassword_failed_count(0);
		}

		// change password
		user.setPassword(new BCryptPasswordEncoder().encode(newPassword));

		userRepository.save(user);

		return null;
	}

	/**
	 * ????????? ?????? ??????
	 * @param userName
	 */
	@Transactional("transactionManager")
	public void duplicateUserName(String userName) {

		if(userName == null || userName.isEmpty())
			throw new BaseException("????????? ????????? ????????????.");

		User user = userRepository.findByUsername(userName);

		if(user != null)
			throw new BaseException("?????? ???????????? ??????????????????.");
	}

	/**
	 * ????????? ?????? ????????? ?????? access token ?????????
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public String getAccessToken(User user) throws Exception {


		final Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "password");
		params.put("client_id", this.clientID);
		params.put("username", user.getUsername());
		params.put("password", user.getPassword());

		final Response response = RestAssured.given()
				.auth()
				.preemptive()
				.basic(this.clientID, this.clientSecret)
				.and()
				.with()
				.params(params)
				.when()
				.post(this.oauthTokenUri);


		logger.debug("[getAccessToken] client id : {}, client secret : {}, oauth uri : {}", this.clientID, this.clientSecret, this.oauthTokenUri);

		String accessToken = "";
		if(response.statusCode() == HttpStatus.SC_OK) {
			accessToken = response.jsonPath().getString("access_token");
		}

		return accessToken;
	}

	/**
	 * ???????????? ??????????????? ????????? ?????? ???????????? ??????
	 * ??????/??????/???????????? ?????? 8??? ?????? 15??? ??????
	 * @return
	 */
	private static String generatePassword() {
		//1~4 : a-z random, 5~6 : time millisecond, 7~12 : uuid 2~8??????, 13~14 : ???????????? random 2??????

		return String.format("%s%s%s%s", randomAlphabetic(4), String.valueOf(System.currentTimeMillis()).substring(11), UUID.randomUUID().toString().substring(2,8), random(2, "!@#$%^&*"));
	}

	/**
	 * @param userName
	 */
	public void checkAuthrize(String userName) {
		OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		String tokenUserName = "";

		if(authentication != null) {
			tokenUserName = authentication.getPrincipal().toString();
		}

		if(userName.equals(tokenUserName) == false)
			throw new BaseException("????????? ???????????????.");
	}
	
	/*
	public TbUserOutlink mergeTbUserOutlink(String ibotId, String adverId, String serviceType) {
		
		//tb_user.user_login == tb_user_outlink.user_login(ibot ?????????)
		//1:1 ???????????? ??????????????????,??????????????? ???????????? ????????? ???????????? update, ????????? insert
		TbUserOutlink tbUserOutlinkData = tbUserOutlinkRepository.findByUserLoginAndServiceType(ibotId, serviceType);
		TbUserOutlink tbUserOutlink = new TbUserOutlink();
		
		if(tbUserOutlinkData == null) {
			logger.error("[UserService.java][mergeTbUserOutlink] tb_user_outlink??? adverId ?????????.");
			tbUserOutlink.setUser_login(ibotId);
			tbUserOutlink.setAdver_id(adverId);
			tbUserOutlink.setService_type(serviceType);
			tbUserOutlink.setCreated_dt(new Date());
			tbUserOutlink.setUpdated_dt(new Date());
			tbUserOutlink.setAgreement001("Y");
			tbUserOutlink = tbUserOutlinkRepository.save(tbUserOutlink);
		} else {
			logger.error("[UserService.java][mergeTbUserOutlink] tb_user_outlink??? adverId ??????.");
			tbUserOutlinkData.setAdver_id(adverId);
			tbUserOutlinkData.setService_type(serviceType);
			tbUserOutlinkData.setUpdated_dt(new Date());
			tbUserOutlinkData.setAgreement001("Y");
			tbUserOutlink = tbUserOutlinkRepository.save(tbUserOutlinkData);
		}
		
		return tbUserOutlink;
	}
	*/
	
	/*
	public TbUserOutlink checkAdverIdDup(String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByAdverIdAndServiceType(adverId, serviceType);
	}
	*/

	/**
	 * @param ibotId
	 * @param serviceType
	 * @return
	 */
	/*
	public TbUserOutlink checkIbotIdDup(String ibotId, String serviceType) {
		return tbUserOutlinkRepository.findByUserLoginAndServiceType(ibotId, serviceType);
	}
	*/
	
	/*
	public TbUserOutlink getTbUserOutlink(String ibotId, String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByUserLoginAndAdverIdAndServiceType(ibotId, adverId, serviceType);
	}
	*/
	
	/*
	public void updateTbUserOutlink(TbUserOutlink tbUserOutlink) {
		tbUserOutlinkRepository.save(tbUserOutlink);
	}
	*/
	
	/*
	public void insertTbUserOutlinkHist(Map<String, Object> map, String resultCode, String resultMessage) {
		String ibotId = (String) Optional.ofNullable(map.get("ibotId")).orElse("");
		String adverId = (String) Optional.ofNullable(map.get("adverId")).orElse("");
		String serviceType = (String) Optional.ofNullable(map.get("serviceType")).orElse("");
		String successYn = (String) Optional.ofNullable(map.get("successYn")).orElse("");
		String requestUrl = (String) Optional.ofNullable(map.get("requestUrl")).orElse("");
		String requestIp = (String) Optional.ofNullable(map.get("requestIp")).orElse("");
		String token = (String) Optional.ofNullable(map.get("token")).orElse("");
		Date expriedDate = (Date) Optional.ofNullable(map.get("expried_date")).orElse(null);
		String agreement001 = (String) Optional.ofNullable(map.get("agreement001")).orElse("");

		TbUserOutlinkHist tbUserOutlinkHist = new TbUserOutlinkHist();
		
		tbUserOutlinkHist.setUser_login(ibotId); //??????
		tbUserOutlinkHist.setAdver_id(adverId);  //??????
		tbUserOutlinkHist.setService_type(serviceType); //??????
		tbUserOutlinkHist.setSuccess_yn(successYn);		//??????
		tbUserOutlinkHist.setCreated_dt(new Date());	//??????
		tbUserOutlinkHist.setUpdated_dt(new Date());	//??????
		tbUserOutlinkHist.setResult_code(resultCode);	//??????
		tbUserOutlinkHist.setResult_message(resultMessage);	//??????
		tbUserOutlinkHist.setRequest_url(requestUrl);		//??????
		tbUserOutlinkHist.setRequest_ip(requestIp);			//??????
		tbUserOutlinkHist.setToken(token);  	  		//??????
		tbUserOutlinkHist.setExpired_dt(expriedDate); 	//??????
		tbUserOutlinkHist.setAgreement001(agreement001);//??????
		tbUserOutlinkHist = tbUserOutlinkHistRepository.save(tbUserOutlinkHist);
	}
	*/
	
	/*
	public TbUserOutlink getTbUserOutlink(String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByAdverIdAndServiceType(adverId, serviceType);
	}
	*/
	
	/**
	 * @param userId
	 * @return
	 */
	/*
	public List<TbUserOutlink> getTbUserOutlinkList(String userId) {
		return tbUserOutlinkRepository.getTbUserOutlinkList(userId);
	}
	*/

	/**
	 * @param userLogin
	 * @return
	 */
	public TbUser getTbUserInfo(String userLogin) {
		return tbUserRepository.findByUserLogin(userLogin);
	}

	/**
	 * @param userLogin
	 * @param accessToken
	 * @return
	 */
	public TbUser findByUserLoginToken(String userLogin, String accessToken) {
		return tbUserRepository.findByUserLoginToken(userLogin, accessToken);
	}

	/**
	 * @param resultTbuser
	 * @return
	 */
	public int modifyUserInfoWithAgreement(TbUser resultTbuser) {
		// ???????????? ??????
		int cnt = tbUserRepository.modifyUserInfoWithAgreement(resultTbuser.getUser_login(), resultTbuser.getAgreement004());
		return cnt;
	}
	
}
