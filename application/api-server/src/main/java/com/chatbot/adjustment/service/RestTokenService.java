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
import org.springframework.stereotype.Service;

import com.chatbot.adjustment.web.common.util.Aes256;
import com.chatbot.adjustment.web.common.util.JSONUtil;
import com.chatbot.adjustment.web.common.vo.CommonVO;
import com.chatbot.adjustment.web.homepage.repository.TbUserRepository;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Service
public class RestTokenService {

	private static final Logger logger = LoggerFactory.getLogger(RestTokenService.class);
	
	@Autowired
	private TbUserRepository tbUserRepository;
	
	String jwtKey = "enliple!1showbot";	// 암호화키값
	
	/**
	 * 토큰 발급
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
		String oauthKey = "client1:secret";
		
		oauthKey = Base64.getEncoder().encodeToString(oauthKey.getBytes());
		//userId = Base64.getEncoder().encodeToString(userId.getBytes());
		//userPass = Base64.getEncoder().encodeToString(userPass.getBytes());
		//logger.info("[createToken] Base64 Encoder username : {}", userId);
		//logger.info("[createToken] Base64 Encoder password : {}", userPass);
		logger.info("[createToken] Base64 Encoder oauthKey : {}", oauthKey);
		
		/*String param = "username=" + userId;
		param += "&password=" + userPass;
		param += "&grant_type=password";*/
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
 	        	
 	        	// tb_user에 토큰 저장
 	        	//Long dateLongTmp = Long.parseLong(result.getAsString("lat")) + Long.parseLong(result.getAsString("expires_in"));
 	        	//Date date = new Date((dateLongTmp * 1000));
 	        	Date today = new Date ();
 	        	Date date = new Date ( today.getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
 	        	
 	        	// 컴퓨터 localtime
 	            String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
 	           logger.info("[createToken] [ " + nowTime + "] [ "+  Thread.currentThread().getName() + "] Update Before Query user id  : " + result.getAsString("sub").toString() + ", token : " + token );
 	        	
 	        	//! 락 오류 발생으로 임시 막음.
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
	 * 토큰 삭제
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
			// tb_user 테이블에서 토큰 삭제
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
	 * 암호화
	 * @param userLogin
	 * @param accessToken
	 * @return
	 */
	public String aesEncrypt(String userLogin, String accessToken) {
		
		Aes256 aes = new Aes256();
		aes.setKey(jwtKey);
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("user_id", userLogin);
		dataMap.put("access_token", accessToken);
		
		String jsonString = JSONUtil.mapToJson(dataMap);
		String encStr = aes.encrypt(jsonString);
		return encStr;
		
	}
	
	/**
	 * 복호화
	 * @param userLogin
	 * @param accessToken
	 * @return
	 * @throws Exception 
	 */
	public JSONObject aesDecrypt(String encDataStr) throws Exception {
		
		Aes256 aes = new Aes256();
		aes.setKey(jwtKey);
		
		String decStr = aes.decrypt(encDataStr);
		JSONParser jparser = new JSONParser();
		JSONObject resultJObj = (JSONObject) jparser.parse(decStr);
		return resultJObj;
		
	}
	
	
	
	
	
}
