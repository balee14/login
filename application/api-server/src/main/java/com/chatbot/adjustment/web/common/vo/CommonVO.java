package com.chatbot.adjustment.web.common.vo;

import lombok.Data;
import net.minidev.json.JSONObject;

@Data
public class CommonVO {
	
	private String userId = null;
	private String userPass = null;
	private String userType = "4";
	private String newUserPass = null;
	private String userName = null;
	private String userTel = null;
	private String companyName = null;
	private String companyNo = null;
	private String companyAddress = null;
	private String email = null;
	private String token = null;
	private String agreement001 = null;
	private String agreement002 = null;
	private String agreement003 = null;
	private String agreement004 = null;
	private String emailTitle = null;
	private String emailText = null;
	private String authNumber = null;
	private String recommendId = null;
	private String searchType = null;
	private long epochTime = 0;
	
	JSONObject dataJObj = new JSONObject();
	
}
