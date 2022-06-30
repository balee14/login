package com.chatbot.adjustment.web.common.util;

import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RestAPIUtil {
	
	static Logger logger = LoggerFactory.getLogger(RestAPIUtil.class);
	static String resultCode = ""; 	//결과코드
	static String resultMsg = ""; 	//결과메시지
	static String content = ""; 	//응답내용
	static int statusCode = 0; 		//상태코드
	
	public JSONObject callOutViserTerminationLink(String url, Map<String, String> valueMap) {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost ;
		httpPost = new HttpPost(url);
    	
		String token = valueMap.get("token"); //tb_user_outlink.token 
    	httpPost.setHeader("authorization", token);
    	
    	JSONObject json = new JSONObject();
    	HttpEntity entry = new StringEntity(json.toString(), "utf-8");
    	httpPost.setEntity(entry);
    	
    	try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
    		content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
    		statusCode = httpResponse.getStatusLine().getStatusCode();
 	        
			JSONParser jparser = new JSONParser();
			json = (JSONObject) jparser.parse(content);
    	} catch (Exception e) {
    		logger.error(e.getMessage());
	        logger.error("[RestAPIUtil.java][callOutViserTerminationLink] call error :::"+e.getMessage());
	        json = null;
	    }
    	
    	return json;
	
	}
	
}
