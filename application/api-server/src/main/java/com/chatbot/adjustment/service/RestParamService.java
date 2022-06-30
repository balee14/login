package com.chatbot.adjustment.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.util.XSSFilter;
import com.chatbot.adjustment.web.common.vo.ResultVO;
import com.chatbot.adjustment.web.common.vo.UserCode;

import net.minidev.json.JSONObject;

@Service
public class RestParamService {

	private static final Logger logger = LoggerFactory.getLogger(RestParamService.class);
	
	@Autowired
	private ResultCodeComponent codeComponent;
	
	Map<String, Object> map = new HashMap<String, Object>();
	
	/**
	 * userId 및 token 체크
	 * @param epochTime
	 * @param dataJObj
	 * @param userId
	 * @param userPass
	 * @param token
	 * @param userType
	 * @return
	 */
	public Map<String, Object> checkParamMap(long epochTime, JSONObject dataJObj, String userId, String userPass, String token, String userType) {
		
		//Map<String, Object> map = new HashMap<String, Object>();
		// userId
		if ( StringUtils.isEmpty(userId) || userId.length() != XSSFilter.XSS(userId).length() ) {
			logger.error("[userLogin] code : USER_0001_CODE");
			logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", dataJObj);
			return map;
		}
		
		// userPass(패스워드에 "&"이 들어 갈 수 있게 때문에 reqParam.getUserPass()은 체크하지 않는다.)
		if(userPass != "off") {
			if ( StringUtils.isEmpty(userPass) ) {
				logger.error("[userLogin] code : USER_0001_CODE");
				logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("accessTime", epochTime);
				map.put("data", dataJObj);
				return map;
			}
			
		}
		
		// token
		if(token != "off") {
			if ( StringUtils.isEmpty(token) ) {
				logger.error("[userLogin] code : USER_0001_CODE");
				logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("accessTime", epochTime);
				map.put("data", dataJObj);
				return map;
			}
		}
		
		// userType
		if(userType != "off") {
			if ( StringUtils.isEmpty(userType) ) {
				logger.error("[userLogin] code : USER_0001_CODE");
				logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("accessTime", epochTime);
				map.put("data", dataJObj);
				return map;
			}
		}
			
		map.put("success", true);
//		map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
//		map.put("accessTime", epochTime);
//		map.put("data", dataJObj);
		return map;
		
	}
	
	/**
	 * @param mode
	 * @param epochTime
	 * @param userId
	 * @param userPass
	 * @param newUserPass
	 * @param userName
	 * @param userTel
	 * @param companyName
	 * @param agreement003
	 * @param companyNo
	 * @return
	 */
	public Map<String, Object> validateParam(String mode, long epochTime, String userId, String userPass, String newUserPass, String email, String userName, String userTel, String companyName, String agreement003, String companyNo) {
		
		logger.error("[validateParam] code : USER_0002_CODE");
		logger.error("[validateParam] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
		map.put("success", false);
		map.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
		map.put("accessTime", epochTime);
		
		// userId check
		if ( userId.length() <= 5 || userId.length() >= 15 ) {
			logger.error("validateParam] => userId Length [len<6 || len>15] : {}", userId.length());
			return map;
		}
		if ( !Pattern.matches("^[a-z0-9]*$", userId) ) {
			logger.error("[validateParam] Error => userId Pattern Matches [^[a-z0-9]*$] : {}", Pattern.matches("^[a-z0-9]*$", userId));
			return map;
		}
		// 등록 및 수정 모드
		if(mode == "I") {
			// 등록
			if ( userPass.length() >= 8 && userPass.length() <= 14 ) {
				int checkUserPass = 0;
				if ( Pattern.matches("^.*(?=.*[0-9]).*$", userPass) ) {
					logger.info("[validateParam] password Pattern Matches [^.*(?=.*[0-9]).*$] : {}", Pattern.matches("^.*(?=.*[0-9]).*$", userPass));
					checkUserPass++;
				}
				if ( Pattern.matches("^.*(?=.*[a-zA-Z]).*$", userPass) ) {
					logger.info("[validateParam] password Pattern Matches [^.*(?=.*[a-zA-Z]).*$] : {}", Pattern.matches("^.*(?=.*[a-zA-Z]).*$", userPass));
					checkUserPass++;
				}
				if ( Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\-_=+\\\\|\\\\[\\\\]{};:\\'\",.<>\\/?`~]).*$", userPass) ) {
					logger.info("[validateParam] password Pattern Matches [^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$] : {}", Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$", userPass));
					checkUserPass++;
				}
				
				// 2가지 이상 조합
				if ( checkUserPass < 2 ) {
					logger.error("[validateParam] Error => password Pattern Matches Check : {}", checkUserPass);
					return map;
				}
			} else {
				logger.error("[validateParam] Error => password Length [len>=8 || len<=14] : {}", userPass.length());
				return map;
			}
		} else {
			// 수정
			if ( !newUserPass.equals("") ) {
				if ( newUserPass.length() >= 8 && newUserPass.length() <= 14 ) {
					int checkUserPass = 0;
					if ( Pattern.matches("^.*(?=.*[0-9]).*$", newUserPass) ) {
						logger.info("[validateParam] newUserPass Pattern Matches [^.*(?=.*[0-9]).*$] : {}", Pattern.matches("^.*(?=.*[0-9]).*$", userPass));
						checkUserPass++;
					}
					if ( Pattern.matches("^.*(?=.*[a-zA-Z]).*$", newUserPass) ) {
						logger.info("[validateParam] newUserPass Pattern Matches [^.*(?=.*[a-zA-Z]).*$] : {}", Pattern.matches("^.*(?=.*[a-zA-Z]).*$", userPass));
						checkUserPass++;
					}
					if ( Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\-_=+\\\\|\\\\[\\\\]{};:\\'\",.<>\\/?`~]).*$", newUserPass) ) {
						logger.info("[validateParam] newUserPass Pattern Matches [^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$] : {}", Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$", userPass));
						checkUserPass++;
					}
					
					// 2가지 이상 조합
					if ( checkUserPass < 2 ) {
						logger.error("[validateParam] Error => newUserPass Pattern Matches Check : {}", checkUserPass);
						return map;
					}
				} else {
					logger.error("[validateParam] Error => newUserPass Length [len>=8 || len<=14] : {}", newUserPass.length());
					return map;
				}
			}
		}
		
		
		// email check
		if ( !Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", email) ) {
			logger.error("[validateParam] Error => email Pattern Matches [^[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*\\\\.[a-zA-Z]{2,6}$] : {}", Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", email));
			return map;
		}
		//
		if ( userName.length() > 30 ) {
			logger.error("[validateParam] Error => userName Length [len>30] : {}", userName.length());
			return map;
		}
		//회원 연락처 8자리 이상('-' 제거하고)
		if ( !StringUtils.isEmpty(userTel) ) {
			if ( 8 <= userTel.replaceAll("-", "").length() ) {
				if ( !Pattern.matches("^[0-9]*$", userTel.replaceAll("-", "")) ) {
					logger.error("[updateUser] Error => userTel Pattern Matches [^[0-9]*$] : {}", Pattern.matches("^[0-9]*$", userTel.replaceAll("-", "")));
					return map;
				}
			} else {
				logger.error("[updateUser] Error => userTel Length [len < 8] : {}", userTel.replaceAll("-", "").length());
				return map;
			}
		}
		// 회사명 check
		if ( companyName.length() > 40 ) {
			logger.error("[validateParam] Error => companyName Length [len>40] : {}", companyName.length());
			return map;
		}
		//
		if ( !StringUtils.isEmpty(agreement003) ) {
			if ( "Y".equals(agreement003) && "N".equals(agreement003) ) {
				logger.error("[validateParam] Error => agreement003 [Y && N] : {}", agreement003);
				return map;
			}
		}
		// 사업자번호 check
		if( !StringUtils.isEmpty(companyNo) ) {
			if ( companyNo.replaceAll("-", "").length() == 10 ) {
				if ( !Pattern.matches("^[0-9]*$", companyNo.replaceAll("-", "")) ) {
					logger.error("[validateParam] Error => companyNo Pattern Matches [^[0-9]*$] :{}", Pattern.matches("^[0-9]*$", companyNo.replaceAll("-", "")));
					return map;
				}
			} else {
				logger.error("[validateParam] Error => companyNo Length [len==10] : {}", companyNo.replaceAll("-", "").length());
				return map;
			}
		}
		//
		map.put("success", true);
		map.put("error", "");
		return map;
		
	}
	
	/**
	 * userId 및 token 체크
	 * @param epochTime
	 * @param dataJObj
	 * @param userId
	 * @param userPass
	 * @param token
	 * @param userType
	 * @return
	 */
	public ResultVO checkParamVo(String userId, String token, String adverId, String serviceType) {
		
		ResultVO paramVO = new ResultVO( null, UserCode.USER_0000_CODE, codeComponent.getCodeMsg(UserCode.USER_0000_CODE) );
		// userId
		if ( StringUtils.isEmpty(userId) || userId.length() != XSSFilter.XSS(userId).length() ) {
			logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
			logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			paramVO.setResultCode(UserCode.USER_0001_CODE);
			paramVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			return paramVO;
		}
		
		// token
		if(token != "off") {
			if ( StringUtils.isEmpty(token) ) {
				logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
				logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				paramVO.setResultCode(UserCode.USER_0001_CODE);
				paramVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
				return paramVO;
			}
		}
		
		// adverId
		if(adverId != "off") {
			if ( StringUtils.isEmpty(adverId) ) {
				logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
				logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				paramVO.setResultCode(UserCode.USER_0001_CODE);
				paramVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
				return paramVO;
			}
		}
		
		// serviceType
		if(serviceType != "off") {
			if ( StringUtils.isEmpty(serviceType) ) {
				logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
				logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				paramVO.setResultCode(UserCode.USER_0001_CODE);
				paramVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
				return paramVO;
			}
		}
		return paramVO;
		
	}
	
	
	
}
