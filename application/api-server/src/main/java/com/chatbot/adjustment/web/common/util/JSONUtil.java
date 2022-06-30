package com.chatbot.adjustment.web.common.util;

import java.util.Map;

import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtil {
	
	public static boolean isJSONValid(String jsonString) {
		try {
			new JSONObject(jsonString);
	    } catch (JSONException e) {
	    	 try {
	             new JSONArray(jsonString);
	         } catch (JSONException ex1) {
	             return false;
	         }
	    }
		return true;
	}

	public static JSONObject convertStringToJson(String jsonString) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonString);
	    } catch (JSONException e) {
	    	
	    }
		return jsonObject;
	}
	
	public static String mapToJson(Map<String, Object> data) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);	// 로그 예쁘게 남기기
			return mapper.writeValueAsString(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
