package com.chatbot.adjustment.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpHeaders;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.vo.CommonVO;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.repository.TbUserRepository;
import com.chatbot.adjustment.web.login.domain.User;
import com.chatbot.adjustment.web.login.domain.code.UserType;
import com.chatbot.adjustment.web.login.repository.UserRepository;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Service
public class RestUserService {

	private static final Logger logger = LoggerFactory.getLogger(RestUserService.class);
	
	@Autowired
	private TbUserRepository tbUserRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ResultCodeComponent codeComponent;
	
	//private Map<String, Object> map = null;
	private Map<String, Object> map = new HashMap<String, Object>();
	
	/**
	 * ID ???????????? API ??????
	 * ???????????? ????????? ??????????????? ?????? ??????
	 * @param user_login
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> idDuplChk(String user_login, long epochTime) throws Exception {
		
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
	
	/**
	 * token ?????? ?????? ??????. ?????? ??? ???????????? ??????
	 * @param epochTime
	 * @param dataJObj
	 * @param userId
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public int countByUserLoginToken(String userId, String token) throws Exception {
		
		// token ??? ???????????? ??????
		int tbUserCnt = tbUserRepository.countByUserLoginToken(userId, token);
		return tbUserCnt;
		
	}
	
	/**
	 * token ?????? ?????? ??????
	 * @param epochTime
	 * @param dataJObj
	 * @param userId
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public TbUser findByUserLoginToken(String userId, String token) throws Exception {
		
		TbUser tbUser = tbUserRepository.findByUserLoginToken(userId, token);
		return tbUser;
		
	}
	
	/**
	 * ???????????? ?????? API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@Transactional("homepageTransactionManager")
	public int updateUser(long epochTime, JSONObject dataJObj, CommonVO reqParam) throws Exception {
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 					//??????
		String token = Optional.ofNullable(reqParam.getToken()).orElse("").trim(); 						//??????
		String userPass = Optional.ofNullable(reqParam.getUserPass()).orElse("").trim(); 				//??????
		String newUserPass = Optional.ofNullable(reqParam.getNewUserPass()).orElse("").trim(); 			//??????
		String email = Optional.ofNullable(reqParam.getEmail()).orElse("").trim(); 						//??????
		String userName = Optional.ofNullable(reqParam.getUserName()).orElse("").trim(); 				//??????
		String userTel = Optional.ofNullable(reqParam.getUserTel()).orElse("").trim(); 					//??????
		String companyName = Optional.ofNullable(reqParam.getCompanyName()).orElse("").trim(); 			//??????
		String agreement003 = Optional.ofNullable(reqParam.getAgreement003()).orElse("").trim(); 		//??????
		String companyNo = Optional.ofNullable(reqParam.getCompanyNo()).orElse("").trim(); 				//??????
		String companyAddress = reqParam.getCompanyAddress().trim(); 									//??????
		
		//String newUserPass = "";
		
		// ???????????? ??????
		TbUser tbUser = tbUserRepository.findByUserLoginToken(userId, token);
		
		TbUser saveTbUser = new TbUser();
		// userId
		saveTbUser.setUser_login(userId);
		// userPass : ?????????????????? ????????? ?????? ?????? ?????? ????????? ?????? ???????????? ????????????
		if ( newUserPass.equals("") ) {
			// ?????? ????????????
			saveTbUser.setUser_pass(tbUser.getUser_pass());
		} else {
			// ?????? ????????????
			newUserPass = new BCryptPasswordEncoder().encode(newUserPass);
			saveTbUser.setUser_pass(newUserPass);
		}
		// email
		if ( email.equals("") ) {
			saveTbUser.setUser_email(tbUser.getUser_email());
		} else {
			saveTbUser.setUser_email(email);
		}
		// userName
		if ( userName.equals("") ) {
			saveTbUser.setUser_name(tbUser.getUser_name());
		} else {
			saveTbUser.setUser_name(userName);
		}
		// userTel
		if ( userTel.equals("") ) {
			saveTbUser.setUser_phone(tbUser.getUser_phone());
		} else {
			saveTbUser.setUser_phone(userTel);
		}
		// companyName
		if ( companyName.equals("") ) {
			saveTbUser.setBiz_name(tbUser.getBiz_name());
		} else {
			saveTbUser.setBiz_name(companyName);
		}
		// agreement003
		if ( agreement003.equals("") ) {
			saveTbUser.setBiz_name(tbUser.getAgreement003());
		} else {
			saveTbUser.setAgreement003(agreement003);
		}
		// companyNo : ?????? ?????? 10??????????????? ??????(??????:111-11-11111)
		if ( companyNo.equals("") ) {
			saveTbUser.setBiz_no(tbUser.getBiz_name());
		} else {
			saveTbUser.setBiz_no(companyNo);
		}
		// companyAddress
		if ( companyAddress.equals("") ) {
			saveTbUser.setBiz_address(tbUser.getBiz_address());
		} else {
			saveTbUser.setBiz_address(companyAddress);
		}
		
		// ???????????? ??????
		int cnt = tbUserRepository.updateTbUserInfo(saveTbUser.getUser_login(), saveTbUser.getUser_pass(), saveTbUser.getBiz_name()
													, saveTbUser.getBiz_no(), saveTbUser.getBiz_address(), saveTbUser.getUser_name()
													, saveTbUser.getUser_email(), saveTbUser.getUser_phone(), saveTbUser.getAgreement003());
		
		if (cnt > 0 && ! StringUtils.isEmpty(userPass) && ! StringUtils.isEmpty(newUserPass)) {
			
			// login.user ???????????? ??????
			User user = userRepository.findByUsername(userId);
			User saveUser = new User();
			saveUser.setUsername(userId);
			saveUser.setPassword_failed_count(user.getPassword_failed_count());
			if (userPass != "" && newUserPass != "") {
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
	 * tb_user ???????????? ??????
	 * @param userId
	 * @param userPass
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * ???????????? ?????? ??????
	 * ???????????? ????????? ??????????????? ?????? ?????? ?????? ??????
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * ?????? ??????
	 * @param userId
	 * @param userPass
	 * @param createTokenUrl
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	/*
	public String createToken(String userId, String userPass, String createTokenUrl) throws UnsupportedEncodingException {
		
		int statusCode = 0;
		String token = null;
		String content = "";
		String oauthKey = "client1:secret";
		
		oauthKey = Base64.getEncoder().encodeToString(oauthKey.getBytes());
		//userId = Base64.getEncoder().encodeToString(userId.getBytes());
		//userPass = Base64.getEncoder().encodeToString(userPass.getBytes());
		//logger.info("[createToken] Base64 Encoder username : {}", userId);
		//logger.info("[createToken] Base64 Encoder password : {}", userPass);
		logger.info("[createToken] Base64 Encoder oauthKey : {}", oauthKey);
		
//		/*String param = "username=" + userId;
//		param += "&password=" + userPass;
//		param += "&grant_type=password";
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("username", userId));
		param.add(new BasicNameValuePair("password", userPass));
		param.add(new BasicNameValuePair("grant_type", "password"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param);
		
		HttpPost httpPost = new HttpPost(createTokenUrl);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + oauthKey);
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
 	        	//Long dateLongTmp = Long.parseLong(result.getAsString("lat")) + Long.parseLong(result.getAsString("expires_in"));
 	        	//Date date = new Date((dateLongTmp * 1000));
 	        	Date today = new Date ();
 	        	Date date = new Date ( today.getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
 	        	
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
	*/

	/**
	 * ?????? ??????
	 * @param commonVo
	 * @param deleteTokenUrl
	 * @return
	 */
	/*
	@SuppressWarnings("unchecked")
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
	*/
	
	
}
