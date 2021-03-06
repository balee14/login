package com.chatbot.adjustment.web.controller.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.adjustment.service.RestExtemalService;
import com.chatbot.adjustment.service.RestParamService;
import com.chatbot.adjustment.service.RestTokenService;
import com.chatbot.adjustment.service.RestUserService;
import com.chatbot.adjustment.service.UserService;
import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.vo.CommonVO;
import com.chatbot.adjustment.web.common.vo.ResultVO;
import com.chatbot.adjustment.web.common.vo.UserCode;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;

import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;

@RestController
public class RestUserController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${login.oauth-token.url:'http://192.168.150.150:8080/login/oauth/token'}")
	private String LOGIN_OAUTH_TOKEN_URL;
	
	@Autowired
	private ResultCodeComponent codeComponent;
	
	@Autowired
	public UserService userService;
	
	@Autowired
	public RestUserService restUserService;
	
	@Autowired
	public RestExtemalService restExtemalService;
	
	@Autowired
	public RestTokenService restTokenService;
	
	@Autowired
	RestParamService restParamService;
	
	private Map<String, Object> map = null;
	
	private Date currentTime = Calendar.getInstance().getTime();
	private long epochTime = currentTime.getTime();
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 2021-12-07 shlee : send ??????????????? ????????? ??? ?????? ?????????????????? ????????? ????????? ?????????.
	 * 
	 * 
	 * 
	 * 
	 */
	
	/**
	 * ???????????? ?????? API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	/*
	@ApiOperation( value = "??????????????????", notes = "????????? : userId, token, userPass, newUserPass, email, userName, userTel, companyName, agreement003"
			+ "\n ????????? : companyNo, companyAddress" )
	@RequestMapping(value="/user/updateUser", method=RequestMethod.POST)
	public Map<String, Object> updateUser(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call updateUser...!");
		logger.info("userId : {}", reqParam.getUserId()); 					//??????
		logger.info("token : {}", reqParam.getToken()); 					//??????
		logger.info("userPass : {}", reqParam.getUserPass()); 				//??????
		logger.info("newUserPass : {}", reqParam.getNewUserPass()); 		//??????
		logger.info("email : {}", reqParam.getEmail()); 					//??????
		logger.info("userName : {}", reqParam.getUserName()); 				//??????
		logger.info("userTel : {}", reqParam.getUserTel()); 				//??????
		logger.info("companyName : {}", reqParam.getCompanyName()); 		//??????
		logger.info("agreement003 : {}", reqParam.getAgreement003()); 		//??????
		logger.info("companyNo : {}", reqParam.getCompanyNo()); 			//??????
		logger.info("companyAddress : {}", reqParam.getCompanyAddress()); 	//??????
		logger.info("**********************************************************");
		
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
		//String companyAddress = reqParam.getCompanyAddress().trim(); 									//??????
		
		JSONObject dataJObj = null;
		
		// ?????? ???????????? ??????
		map = restParamService.checkParamMap(epochTime, dataJObj, userId, userPass, token, "off");
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// ????????? ??? ???????????? ?????? ??????
		map = restParamService.validateParam("U", epochTime, userId, userPass, newUserPass, email, userName, userTel, companyName, agreement003, companyNo);
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// ???????????? ??????
		TbUser resultUserInfo = restUserService.selectTbUserInfoWithPass(userId, userPass);
		logger.info("[userLogin] resultUserInfo : {}", resultUserInfo);
		if ( resultUserInfo == null ) {
			logger.error("[userLogin] code : USER_4000_CODE");
			logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", dataJObj);
			return map;
		}
		
		// token ?????? ?????? ??????
		int tbUserCnt = restUserService.countByUserLoginToken(userId, token);
		if(tbUserCnt < 1) {
			logger.error("[userLogin] code : USER_4000_CODE");
			logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", dataJObj);
			return map;
		}
		
		// login.user, tb_user??? ?????? ??????
		int resultCnt = restUserService.updateUser(epochTime, dataJObj, reqParam);
		if ( resultCnt > 0 ) {
			logger.info("[updateUser] code : USER_0000_CODE");
			logger.info("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
			map.put("success", true);
			map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
			map.put("accessTime", epochTime);
			return map;
		} else {
			logger.error("[updateUser] code : USER_3000_CODE");
			logger.error("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_3000_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_3000_CODE"));
			map.put("accessTime", epochTime);
			return map;
		}
		
	}
	*/
	
	/**
	 * ???????????? ?????? API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	/*
	@ApiOperation(value = "??????????????????", notes = "????????? : userId, token")
	@RequestMapping(value="/user/getUserInfo", method=RequestMethod.POST)
	public Map<String, Object> getUserInfo(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call userInfo...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("token : {}", reqParam.getToken());
		logger.info("**********************************************************");
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 		//??????
		String token = Optional.ofNullable(reqParam.getToken()).orElse("").trim(); 			//??????
		
		JSONObject dataJObj = null;
		
		// ???????????? param?????? ??????
		map = restParamService.checkParamMap(epochTime, dataJObj, userId, "off", token, "off");
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// token ?????? ?????? ??????
		int tbUserCnt = restUserService.countByUserLoginToken(userId, token);
		if(tbUserCnt < 1) {
			logger.error("[userLogin] code : USER_4000_CODE");
			logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", dataJObj);
			return map;
		}
		
		// ???????????? ??????
		TbUser resultTbuser = restUserService.findByUserLoginToken(userId, token);
		if ( resultTbuser != null ) {
			logger.info("[userInfo] code : USER_0000_CODE");
			logger.info("[userInfo] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
			JSONObject returnDateJObj = new JSONObject();
			returnDateJObj.put("userId", resultTbuser.getUser_login());
			returnDateJObj.put("companyName", resultTbuser.getBiz_name());
			returnDateJObj.put("companyNo", resultTbuser.getBiz_no());
			returnDateJObj.put("companyAddress", resultTbuser.getBiz_address());
			returnDateJObj.put("bossName", resultTbuser.getBoss_name());
			returnDateJObj.put("userName", resultTbuser.getUser_name());
			returnDateJObj.put("email", resultTbuser.getUser_email());
			returnDateJObj.put("userTel", resultTbuser.getUser_phone());
			returnDateJObj.put("agreement001", Optional.ofNullable(resultTbuser.getAgreement001()).orElse("N"));
			returnDateJObj.put("agreement002", Optional.ofNullable(resultTbuser.getAgreement002()).orElse("N"));
			returnDateJObj.put("agreement003", Optional.ofNullable(resultTbuser.getAgreement003()).orElse("N"));
			returnDateJObj.put("agreement004", Optional.ofNullable(resultTbuser.getAgreement004()).orElse("N"));
			returnDateJObj.put("ssoUserId", resultTbuser.getSso_user_id());
			//sso_user_role ::: 1 = system ?????? ??????, 4 = admin ?????? ??????
			returnDateJObj.put("ssoUserRole", resultTbuser.getSso_user_role()); 
			// ?????????????????? ?????? (????????????????????? ?????? ??????????????? ?????? ??????(??????????????? ?????? ?????? ??? ??????))
			List<TbUserOutlink> tbUserOutlinkInfo = restExtemalService.getTbUserOutlinkList(reqParam.getUserId());
			if(tbUserOutlinkInfo.size() > 0) {
				returnDateJObj.put("adverIds", tbUserOutlinkInfo);
			} else {
				returnDateJObj.put("adverIds", null);
			}
			map.put("data", returnDateJObj);
			map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
			map.put("accessTime", epochTime);
			map.put("success", true);
			return map;
		} else {
			logger.error("[userInfo] code : USER_4000_CODE");
			logger.error("[userInfo] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("data", null);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("success", false);
			return map;
		}
		
	}
	*/
	
	/**
	 * ????????? ??????(access_token) ????????? API (????????????????????? ??????)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	/*
	@CrossOrigin(origins="*")
	@ApiOperation( value = "????????? ??????(access_token) ?????????", notes = "????????? : userId, token" )
	@RequestMapping(value="/user/getEncData", method=RequestMethod.POST)
	public ResultVO getEncData(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][getEncData] userId : {}", reqParam.getUserId());
		logger.info("[RestApiController.java][getEncData] token : {}", reqParam.getToken());
		logger.info("**********************************************************");
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 		//??????
		String token = Optional.ofNullable(reqParam.getToken()).orElse("").trim(); 			//??????
		
		ResultVO resultVO = new ResultVO( null, UserCode.USER_0000_CODE, codeComponent.getCodeMsg(UserCode.USER_0000_CODE) );
		
		// ???????????? param?????? ??????
		resultVO = restParamService.checkParamVo(userId, token, "off", "off");
		if(resultVO.getResultCode() == UserCode.USER_0001_CODE) {
			return resultVO;
		}
		
		// token ?????? ?????? ??????
		int tbUserCnt = restUserService.countByUserLoginToken(userId, token);
		if(tbUserCnt < 1) {
			logger.error("[getEncData] code : USER_4000_CODE");
			logger.error("[getEncData] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			resultVO.setResultCode(UserCode.USER_4000_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_4000_CODE"));
			return resultVO;
		} 
		
		try {
			
			String encStr = restTokenService.aesEncrypt(userId, token);
			map = new HashMap<String, Object>();
			map.put("enc", encStr);
			resultVO.setItems(map);
			return resultVO;
			
		} catch(Exception e) {
			
			e.printStackTrace();
			logger.error("[RestApiController.java][getEncData] code : USER_9999_CODE");
			logger.error("[RestApiController.java][getEncData] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			return resultVO;
			
		}
		
	}
	*/
	
	
	
}
