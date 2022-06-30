package com.chatbot.adjustment.web.controller.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.adjustment.service.MailService;
import com.chatbot.adjustment.service.MallService;
import com.chatbot.adjustment.service.RestExtemalService;
import com.chatbot.adjustment.service.RestParamService;
import com.chatbot.adjustment.service.RestTokenService;
import com.chatbot.adjustment.service.RestUserService;
import com.chatbot.adjustment.service.UserService;
import com.chatbot.adjustment.web.adjustment.domain.ServiceInfo;
import com.chatbot.adjustment.web.adjustment.domain.code.PaymentType;
import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.exception.AuthError;
import com.chatbot.adjustment.web.common.util.CommonUtils;
import com.chatbot.adjustment.web.common.util.XSSFilter;
import com.chatbot.adjustment.web.common.vo.CommonVO;
import com.chatbot.adjustment.web.common.vo.ResultVO;
import com.chatbot.adjustment.web.common.vo.UserCode;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;
import com.chatbot.adjustment.web.login.domain.User;
import com.chatbot.adjustment.web.login.domain.code.UserType;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import net.minidev.json.JSONObject;

@RestController
public class RestApiController {

	private static final Logger logger = LoggerFactory.getLogger(RestApiController.class);
	
	@Autowired
	private ResultCodeComponent codeComponent;
	
	@Autowired
	RestParamService restParamService;
	
	@Autowired
	RestTokenService restTokenService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RestUserService restUserService;
	
	@Autowired
	MallService mallService;
	
	@Autowired
	private RestExtemalService restExtemalService;

	@Autowired
	private MailService mailService;
	
	@Value("${login.oauth-token.url:'http://192.168.150.150:8080/login/oauth/token'}")
	private String LOGIN_OAUTH_TOKEN_URL;
	
	@Value("${login.revoke-token.url:'http://192.168.150.150:8080/login/oauth/revoke-token'}")
	private String LOGIN_REVOKE_TOKEN_URL;
	
	@Value("${mobon.api.check-user-url:'http://api.mobon.net:9981/api/dspt/external/advertiser/checkUser'}")
	private String MOBON_API_CHECK_USER_URL;
	
	@Value("${mobon.api.create-url:'http://api.mobon.net:9981/api/dspt/external/advertiser/create'}")
	private String MOBON_API_CREATE_URL;
	
	@Value("${mobon.api.checkPasswd-url:'http://api.mobon.net:9981/api/dspt/external/advertiser/checkPasswd'}")
	private String MOBON_API_CHECKPASSWD_URL;

	@Value("${mobon.api.update-url:'http://api.mobon.net:9981/api/dspt/external/advertiser/update'}")
	private String MOBON_API_UPDATE_URL;
	
	@Value("${mobon.api.detail-url:'http://api.mobon.net:9981/api/dspt/external/advertiser/detail'}")
	private String MOBON_API_DETAIL_URL;
	
	@Value("${login.token.key:'enliple!1ibot'}")
	private String LOGIN_TOKEN_KEY;
	
	@Value("${viser.api.terminate-user-url:'http://192.168.150.8:8080/api/account/terminationLink'}")
	private String VISER_API_TERMINATE_USER_URL;
	
	private Map<String, Object> map = null;
	
	private Date currentTime = Calendar.getInstance().getTime();
	private long epochTime = currentTime.getTime();
	
	/**
	 * ID 중복체크 API
	 * 2021-12-02 shlee : 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "ID 중복체크 (아이봇아이디 중복체크)", notes = "필수값 : userId" )
	@RequestMapping(value="/api/idDuplChk", method=RequestMethod.POST)
	public Map<String, Object> idDuplChk(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call idDuplChk...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("**********************************************************");
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 		//필수
		
		JSONObject dataJObj = null;
		
		// 정상적인 param인지 체크
		map = restParamService.checkParamMap(epochTime, dataJObj, userId, "off", "off", "off");
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// 중복체크
		map = restUserService.idDuplChk(reqParam.getUserId(), epochTime);
		return map;
		
	}
	
	/**
	 * 회원가입 API
	 * 2021-12-02 shlee : 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원가입", notes = "필수값 : userId, userPass, userType, email, userName, userTel, companyName, agreement001, agreement002, agreement003 "
			+ "\n 옵션값 : companyNo, companyAddress, agreement004, recommendId(추천인아이디로 값이 있을 때만 사용,숫자로된 유니크한 유저아이디)" )
	@RequestMapping(value="/api/registerUser", method=RequestMethod.POST)
	public Map<String, Object> registerUser(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call registerUser...!");
		logger.info("userId : {}", reqParam.getUserId()); 					//필수
		logger.info("userPass : {}", reqParam.getUserPass()); 				//필수
		logger.info("userType : {}", reqParam.getUserType()); 				//필수
		logger.info("email : {}", reqParam.getEmail()); 					//필수
		logger.info("userName : {}", reqParam.getUserName()); 				//필수
		logger.info("userTel : {}", reqParam.getUserTel()); 				//필수
		logger.info("companyName : {}", reqParam.getCompanyName()); 		//필수
		logger.info("companyNo : {}", reqParam.getCompanyNo()); 			//옵션
		logger.info("companyAddress : {}", reqParam.getCompanyAddress()); 	//옵션
		logger.info("agreement001 : {}", reqParam.getAgreement001()); 		//필수
		logger.info("agreement002 : {}", reqParam.getAgreement002()); 		//필수
		logger.info("agreement003 : {}", reqParam.getAgreement003()); 		//필수
		logger.info("agreement004 : {}", reqParam.getAgreement004()); 		//옵션
		logger.info("recommendId : {}", reqParam.getRecommendId()); 		//옵션
		logger.info("**********************************************************");
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 					//필수
		String userPass = Optional.ofNullable(reqParam.getUserPass()).orElse("").trim(); 				//필수
		String userType = Optional.ofNullable(reqParam.getUserType()).orElse("").trim(); 				//필수
		//userType 4는 유효성 체크
		userType = Optional.ofNullable(userType).orElse("4");
		if(userType.trim().isEmpty()) {
			userType = "4";
		}
		reqParam.setUserType(userType);
		String email = Optional.ofNullable(reqParam.getEmail()).orElse("").trim(); 						//필수
		String userName = Optional.ofNullable(reqParam.getUserName()).orElse("").trim(); 				//필수
		String userTel = Optional.ofNullable(reqParam.getUserTel()).orElse("").trim(); 					//필수
		String companyName = Optional.ofNullable(reqParam.getCompanyName()).orElse("").trim(); 			//필수
		String companyNo = Optional.ofNullable(reqParam.getCompanyNo()).orElse("").trim(); 				//옵션
		//String companyAddress = reqParam.getCompanyAddress().trim(); 									//옵션
		String agreement001 = Optional.ofNullable(reqParam.getAgreement001()).orElse("").trim(); 		//필수
		String agreement002 = Optional.ofNullable(reqParam.getAgreement002()).orElse("").trim(); 		//필수
		String agreement003 = Optional.ofNullable(reqParam.getAgreement003()).orElse("").trim(); 		//필수
		//String agreement004 = reqParam.getAgreement004().trim(); 										//옵션
		//String recommendId = reqParam.getRecommendId().trim(); 										//옵션
		
		JSONObject dataJObj = null;
		
		//userId, userPass, userType 만은 필필수입니다.!!!!!
		// 정상적인 param인지 체크
		map = restParamService.checkParamMap(epochTime, dataJObj, userId, userPass, "off", userType);
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		// 자리수 및 특수문자 처리 등등
		map = restParamService.validateParam("I", epochTime, userId, userPass, null, email, userName, userTel, companyName, agreement003, companyNo);
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// 필수값 유효성 체크
		if (StringUtils.isEmpty(email)
			|| StringUtils.isEmpty(userName)
			|| StringUtils.isEmpty(userTel)
			|| StringUtils.isEmpty(companyName)
			|| StringUtils.isEmpty(agreement001)
			|| StringUtils.isEmpty(agreement002)
			|| StringUtils.isEmpty(agreement003)) {
			
			logger.info("[registerUser] code : USER_0001_CODE");
			logger.info("[registerUser] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			return map;
			
		}
		
		//userType 1,2,3,5은 바로 회원가입 (userType 필수값으로 받기)
		if ( reqParam.getUserType().equals( UserType.ROLE_HOMEMANAGER.getCode() ) 
				|| reqParam.getUserType().equals( UserType.ROLE_MANAGER.getCode() ) 
				|| reqParam.getUserType().equals( UserType.ROLE_USER.getCode() )
				|| reqParam.getUserType().equals( UserType.ROLE_HOMEPAGEADMIN.getCode() )) {
			
			// 회원 등록
			int resultCode = restUserService.registerUser(reqParam);
			if (resultCode > 0) { 	//유저등록 성공
				logger.info("[registerUser] code : USER_0000_CODE");
				logger.info("[registerUser] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("success", true);
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
				return map;
			} else {				//유저등록 실패 
				logger.error("[registerUser] code : USER_2000_CODE");
				logger.error("[registerUser] msg : {}", codeComponent.getCodeMsg("USER_2000_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_2000_CODE"));
				map.put("accessTime", epochTime);
				return map;
			}
			
		} else {
			
			// 아이디 중복 체크 API(/api/dspt/external/advertiser/checkUser) 호출
			map = restUserService.idDuplChk(userId, epochTime);
			if(map.get("success").equals("true") || map.get("success").equals(true)) {
				// 중복이 아닐 경우
				int resultCode = restUserService.registerUser(reqParam);
				if (resultCode > 0) {
					logger.info("[registerUser] code : USER_0000_CODE");
					logger.info("[registerUser] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
					map.put("success", true);
					map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
					map.put("accessTime", epochTime);
				} else {
					logger.error("[registerUser] code : USER_2000_CODE");
					logger.error("[registerUser] msg : {}", codeComponent.getCodeMsg("USER_2000_CODE"));
					map.put("success", false);
					map.put("error", codeComponent.getCodeMsg("USER_2000_CODE"));
					map.put("accessTime", epochTime);
				}
				return map;
			} else {
				// 중복인 경우
				return map;
			}
			
		}
		
	}
	
	/**
	 * 회원정보 수정 API
	 * 2021-12-02 shlee : RestUserController 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원정보수정", notes = "필수값 : userId, userPass, newUserPass, companyName, userName, email, userTel, agreement003"
			+ "\n 옵션값 : companyNo, companyAddress" )
	@RequestMapping(value="/api/updateUser", method=RequestMethod.POST)
	public Map<String, Object> updateUser(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call updateUser...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("userName : {}", reqParam.getUserName());
		logger.info("userTel : {}", reqParam.getUserTel());
		logger.info("companyName : {}", reqParam.getCompanyName());
		logger.info("companyNo : {}", reqParam.getCompanyNo());
		logger.info("companyAddress : {}", reqParam.getCompanyAddress());
		logger.info("email : {}", reqParam.getEmail());
		logger.info("agreement003 : {}", reqParam.getAgreement003());
		logger.info("**********************************************************");
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//Map<String, Object> dataMap = new HashMap<String, Object>();
		
		Date currentTime = Calendar.getInstance().getTime();
		long epochTime = currentTime.getTime();
		returnMap.put("accessTime", epochTime);
		
		// 유효성 체크
		int isOk = 1;
		if ( StringUtils.isEmpty(reqParam.getUserId()) ) {
			isOk = 0;
		}
		if ( reqParam.getUserId().length() < 4 || reqParam.getUserId().length() > 14 ) {
			logger.error("[updateUser] Error => userId Length [len<4 || len>14] : {}", reqParam.getUserId().length());
			isOk = -1;
		}
		if ( !Pattern.matches("^[a-z0-9]*$", reqParam.getUserId()) ) {
			logger.error("[updateUser] Error => userId Pattern Matches [^[a-z0-9]*$] : {}", Pattern.matches("^[a-z0-9]*$", reqParam.getUserId()));
			isOk = -1;
		}
		if ( !StringUtils.isEmpty(reqParam.getUserPass()) ) {
			if ( StringUtils.isEmpty(reqParam.getNewUserPass()) ) {
				isOk = 0;
			}
		}
		if ( !StringUtils.isEmpty(reqParam.getNewUserPass()) ) {
			if ( StringUtils.isEmpty(reqParam.getUserPass()) ) {
				isOk = 0;
			}
		}
		if ( !StringUtils.isEmpty(reqParam.getUserPass()) && ! StringUtils.isEmpty(reqParam.getNewUserPass()) ) {
			if ( reqParam.getNewUserPass().length() >= 8 && reqParam.getNewUserPass().length() <= 14 ) {
				int checkUserPass = 0;
				if ( Pattern.matches("^.*(?=.*[0-9]).*$", reqParam.getNewUserPass()) ) {
					logger.info("[registerUser] newUserPass Pattern Matches [^.*(?=.*[0-9]).*$] : {}", Pattern.matches("^.*(?=.*[0-9]).*$", reqParam.getUserPass()));
					checkUserPass++;
				}
				if ( Pattern.matches("^.*(?=.*[a-zA-Z]).*$", reqParam.getNewUserPass()) ) {
					logger.info("[registerUser] newUserPass Pattern Matches [^.*(?=.*[a-zA-Z]).*$] : {}", Pattern.matches("^.*(?=.*[a-zA-Z]).*$", reqParam.getUserPass()));
					checkUserPass++;
				}
				if ( Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\-_=+\\\\|\\\\[\\\\]{};:\\'\",.<>\\/?`~]).*$", reqParam.getNewUserPass()) ) {
					logger.info("[registerUser] newUserPass Pattern Matches [^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$] : {}", Pattern.matches("^.*(?=.*[!@#$%^&*()\\\\\\\\-_=+\\\\\\\\|\\\\\\\\[\\\\\\\\]{};:\\\\'\\\",.<>\\\\/?`~]).*$", reqParam.getUserPass()));
					checkUserPass++;
				}
				
				// 2가지 이상 조합
				if ( checkUserPass < 2 ) {
					logger.error("[updateUser] Error => newUserPass Pattern Matches Check : {}", checkUserPass);
					isOk = -1;
				}
			} else {
				logger.error("[updateUser] Error => newUserPass Length [len>=8 || len<=14] : {}", reqParam.getNewUserPass().length());
				isOk = -1;
			}
		}
		if ( !StringUtils.isEmpty(reqParam.getCompanyAddress()) ) {
			if ( reqParam.getCompanyAddress().length() != XSSFilter.XSS(reqParam.getCompanyAddress()).length() ) {
				logger.error("[updateUser] Error => companyAddress Length [len == len(XSSFilter)] : {},{}", reqParam.getCompanyAddress().length(), XSSFilter.XSS(reqParam.getCompanyAddress()).length());
				isOk = -1;
			}
		}
		if( !StringUtils.isEmpty(reqParam.getCompanyNo()) ) {
			if ( reqParam.getCompanyNo().replaceAll("-", "").length() == 10 ) {
				if ( !Pattern.matches("^[0-9]*$", reqParam.getCompanyNo().replaceAll("-", "")) ) {
					logger.error("[updateUser] Error => companyNo Pattern Matches [^[0-9]*$] :{}", Pattern.matches("^[0-9]*$", reqParam.getCompanyNo().replaceAll("-", "")));
					isOk = -1;
				}
			} else {
				logger.error("[updateUser] Error => companyNo Length [len==10] : {}", reqParam.getCompanyNo().replaceAll("-", "").length());
				isOk = -1;
			}
		}
		if ( !StringUtils.isEmpty(reqParam.getEmail()) ) {
			if ( !Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", reqParam.getEmail()) ) {
				logger.error("[updateUser] Error => email Pattern Matches [^[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*\\\\.[a-zA-Z]{2,6}$] : {}", Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", reqParam.getEmail()));
				isOk = -1;
			}
		}
		//회원 연락처 8자리 이상('-' 제거하고)
		if ( !StringUtils.isEmpty(reqParam.getUserTel()) ) {
			if ( 8 <= reqParam.getUserTel().replaceAll("-", "").length() ) {
				if ( !Pattern.matches("^[0-9]*$", reqParam.getUserTel().replaceAll("-", "")) ) {
					logger.error("[updateUser] Error => userTel Pattern Matches [^[0-9]*$] : {}", Pattern.matches("^[0-9]*$", reqParam.getUserTel().replaceAll("-", "")));
					isOk = -1;
				}
			} else {
				logger.error("[updateUser] Error => userTel Length [len < 8] : {}", reqParam.getUserTel().replaceAll("-", "").length());
				isOk = -1;
			}
		}
		if ( !StringUtils.isEmpty(reqParam.getAgreement003()) ) {
			if ( "Y".equals(reqParam.getAgreement003()) && "N".equals(reqParam.getAgreement003()) ) {
				logger.error("[updateUser] Error => agreement003 [Y && N] : {}", reqParam.getAgreement003());
				isOk = -1;
			}
		}
		if ( isOk == 0 ) {
			logger.info("[updateUser] code : USER_0001_CODE");
			logger.info("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			returnMap.put("success", false);
			returnMap.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			returnMap.put("accessTime", epochTime);
			return returnMap;
		}
		if ( isOk == -1 ) {
			logger.error("[updateUser] code : USER_0002_CODE");
			logger.error("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
			returnMap.put("success", false);
			returnMap.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
			returnMap.put("accessTime", epochTime);
			return returnMap;
		}
		
		// 다중로그인.. 주석처리
		// ID와 token이 맞는지 조회
//			int cnt = userService.selectIsUser(reqParam);
//			if (cnt > 0) {
			// 비밀번호도 변경한다면 비밀번호가 맞는지 체크
			if (! StringUtils.isEmpty(reqParam.getUserPass()) && ! StringUtils.isEmpty(reqParam.getNewUserPass())) {
				// 아이디/비밀번호 API(/api/dspt/external/advertiser/checkPasswd) 호출
				// 모비온과 회원을 구분하기로 하여 주석처리
//					dataMap = userService.userLogin(reqParam, MOBON_API_CHECKPASSWD_URL);
//					if (! "true".equals(dataMap.get("success").toString())) {
//						returnMap.put("success", false);
//						returnMap.put("error", "userLogin call fail");
//						return returnMap;
//					}
				
				// ID와 비밀번호 체크
				TbUser resultIsUser = restUserService.selectTbUserInfoWithPass(reqParam.getUserId(), reqParam.getUserPass());
				if (resultIsUser == null) {
					logger.error("[updateUser] code : USER_0003_CODE");
					logger.error("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_0003_CODE"));
					returnMap.put("success", false);
					returnMap.put("error", codeComponent.getCodeMsg("USER_0003_CODE"));
					return returnMap;
				}
			}
			
			// 회원정보 조회 API(/api/dspt/external/advertiser/detail) 호출
			// 모비온과 회원을 구분하기로 하여 주석처리
//				dataMap = userService.getUserInfo(reqParam.getUserId(), MOBON_API_DETAIL_URL);
//				if ("true".equals(dataMap.get("success").toString())) {
//					JSONParser jparser = new JSONParser();
//					JSONObject dataJObj = (JSONObject) jparser.parse(dataMap.get("data").toString());
//					if (StringUtils.isEmpty(reqParam.getCompanyName())) {
//						reqParam.setCompanyName(dataJObj.getAsString("companyName"));
//					}
//					if (StringUtils.isEmpty(reqParam.getCompanyNo())) {
//						reqParam.setCompanyNo(dataJObj.getAsString("companyNo"));
//					}
//					if (StringUtils.isEmpty(reqParam.getCompanyAddress())) {
//						reqParam.setCompanyAddress(dataJObj.getAsString("companyAddress"));
//					}
//					if (StringUtils.isEmpty(reqParam.getUserName())) {
//						reqParam.setUserName(dataJObj.getAsString("userName"));
//					}
//					if (StringUtils.isEmpty(reqParam.getEmail())) {
//						reqParam.setEmail(dataJObj.getAsString("email"));
//					}
//					if (StringUtils.isEmpty(reqParam.getUserTel())) {
//						reqParam.setUserTel(dataJObj.getAsString("userTel"));
//					}

				// 회원정보 수정 API(/api/dspt/external/advertiser/update) 호출
				// 모비온과 회원을 구분하기로 하여 주석처리
//					dataMap = userService.userUpdate(reqParam, MOBON_API_UPDATE_URL);
//					if ("true".equals(dataMap.get("success").toString())) {
					// login.user, tb_user에 정보 수정
					int resultCnt = userService.updateUser(reqParam);
					if ( resultCnt > 0 ) {
						logger.info("[updateUser] code : USER_0000_CODE");
						logger.info("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
						returnMap.put("success", true);
						returnMap.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
					} else {
						logger.error("[updateUser] code : USER_3000_CODE");
						logger.error("[updateUser] msg : {}", codeComponent.getCodeMsg("USER_3000_CODE"));
						returnMap.put("success", false);
						returnMap.put("error", codeComponent.getCodeMsg("USER_3000_CODE"));
					}
//					} else {
//						returnMap.put("success", false);
//						returnMap.put("error", "userUpdate call fail");
//					}
//				} else {
//					returnMap.put("success", false);
//					returnMap.put("error", "getUserInfo call fail");
//				}
//			} else {
//				returnMap.put("success", false);
//				returnMap.put("error", "updateUser fail");
//			}
		
		return returnMap;
	}
	
	/**
	 * 회원정보 조회 API
	 * 2021-12-02 shlee : RestUserController 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원정보조회", notes = "필수값 : userId" )
	@RequestMapping(value="/api/getUserInfo", method=RequestMethod.POST)
	public Map<String, Object> getUserInfo(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call userInfo...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("token : {}", reqParam.getToken());
		logger.info("**********************************************************");
		
		Map<String, Object> map = new HashMap<String, Object>();
		//JSONObject dataJObj = null;
		
		Date currentTime = Calendar.getInstance().getTime();
		long epochTime = currentTime.getTime();
		
		// 파라미터 체크
		if ( StringUtils.isEmpty(reqParam.getUserId()) ) {
			logger.error("[userInfo] code : USER_0001_CODE");
			logger.error("[userInfo] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		if ( reqParam.getUserId().length() != XSSFilter.XSS(reqParam.getUserId()).length() ) {
			logger.error("[userInfo] code : USER_0002_CODE");
			logger.error("[userInfo] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		
		// 다중로그인.. 주석처리
		// ID와 token이 맞는지 조회
//			int cnt = userService.selectIsUser(reqParam);
//			
//			if (cnt > 0) {
//				
//				// 회원정보 조회 API(/api/dspt/external/advertiser/detail) 호출
//				map = userService.getUserInfo(reqParam.getUserId(), "http://api.mobon.net:9981/api/dspt/external/advertiser/detail");
//			} else {
//				map.put("accessTime", epochTime);
//				map.put("success", false);
//				map.put("error", "getUserInfo fail");
//				map.put("data", dataJObj);
//			}
		
		// 회원정보 조회 API(/api/dspt/external/advertiser/detail) 호출
		// 모비온과 회원을 구분하기로 하여 주석처리
//			map = userService.getUserInfo(reqParam.getUserId(), MOBON_API_DETAIL_URL);
		
		//ApiMyPageController.java
		//lucy-web에 있는 /api/v1/mypage/selectMyPageUserInfo API 와 동일
		// 회원정보 조회
		TbUser resultTbuser = userService.selectTbUserInfo(reqParam.getUserId());
		// 회원연동정보 조회 (아이봇아이디로 인해 외부연동된 정보 조회(서비스별로 여러 개일 수 있음))
		List<TbUserOutlink> tbUserOutlinkInfo = restExtemalService.getTbUserOutlinkList(reqParam.getUserId()); 
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
			returnDateJObj.put("ssoUserRole", resultTbuser.getSso_user_role()); 
			//sso_user_role ::: 1 = system 계정 권한, 4 = admin 계정 권한
			
			if(tbUserOutlinkInfo.size() > 0) {
				returnDateJObj.put("adverIds", tbUserOutlinkInfo);
			} else {
				returnDateJObj.put("adverIds", null);
			}
			
			map.put("data", returnDateJObj);
			map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
			map.put("accessTime", epochTime);
			map.put("success", true);
		} else {
			logger.error("[userInfo] code : USER_4000_CODE");
			logger.error("[userInfo] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("data", null);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("success", false);
		}
		
		return map;
	}
	
	/**
	 * 로그인 API
	 * 2021-12-02 shlee : 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "유저로그인", notes = "필수값 : userId, userPass" )
	@RequestMapping(value="/api/userLogin", method=RequestMethod.POST)
	public Map<String, Object> userLogin(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call userLogin...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("userPass : {}", reqParam.getUserPass());
		logger.info("**********************************************************");
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("").trim(); 		//필수
		String userPass = Optional.ofNullable(reqParam.getUserPass()).orElse("").trim(); 	//필수
		
		JSONObject dataJObj = null;
		
		// 정상적인 param인지 체크
		map = restParamService.checkParamMap(epochTime, dataJObj, userId, userPass, "off", "off");
		if(map.get("success").equals("false") || map.get("success").equals(false)) {
			return map;
		}
		
		// tb_user에 유저가 있는지 조회
		TbUser resultUserInfo = restUserService.selectTbUserInfoWithPass(reqParam.getUserId(), reqParam.getUserPass());
		logger.info("[userLogin] resultUserInfo : {}", resultUserInfo);
		if ( resultUserInfo != null ) {
			// 토큰 발급하기
			String token = restTokenService.createToken(reqParam.getUserId(), reqParam.getUserPass(), LOGIN_OAUTH_TOKEN_URL);
			if ( !StringUtils.isEmpty(token) ) {
				dataJObj = new JSONObject();
				dataJObj.put("token", null);
				dataJObj.replace("token", token);
				map.put("success", true);
				map.put("error", codeComponent.getCodeMsg("USER_0000_CODE"));
				map.put("accessTime", epochTime);
				map.put("data", dataJObj);
			} else {
				logger.error("[userLogin] code : USER_5000_CODE");
				logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_5000_CODE"));
				map.put("success", false);
				map.put("error", codeComponent.getCodeMsg("USER_5000_CODE"));
				map.put("accessTime", epochTime);
				map.put("data", dataJObj);
			}
		} else {
			logger.error("[userLogin] code : USER_4000_CODE");
			logger.error("[userLogin] msg : {}", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_4000_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", dataJObj);
		}
		logger.info("[userLogin] token : {}", map.get("data"));
		return map;
		
	}
	
	/**
	 * 로그아웃 API
	 * 2021-12-02 shlee : LOGIN_REVOKE_TOKEN_URL 호출이 안되서 오류 발생함.
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "유저 로그아웃", notes = "필수값 : userId, token" )
	@RequestMapping(value="/api/userLogout", method=RequestMethod.POST)
	public Map<String, Object> userLogout(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call userLogout...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("userPass : {}", reqParam.getUserPass());
		logger.info("**********************************************************");
		
		//Map<String, Object> map = new HashMap<String, Object>();
		
		//Date currentTime = Calendar.getInstance().getTime();
		//long epochTime = currentTime.getTime();
		
		// 유효성 체크
		if ( StringUtils.isEmpty(reqParam.getUserId()) ) {
			logger.error("[userLogout] code : USER_0001_CODE");
			logger.error("[userLogout] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		if ( reqParam.getUserId().length() != XSSFilter.XSS(reqParam.getUserId()).length() ) {
			logger.error("[userLogout] code : USER_0002_CODE");
			logger.error("[userLogout] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		
		// 토큰 삭제하기
		map = restTokenService.deleteToken(reqParam, LOGIN_REVOKE_TOKEN_URL);
		
		return map;
	}	
		
	/**
	 * 아이디찾기 API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "아이디 찾기", notes = "필수값 : companyName, companyNo, email" )
	@RequestMapping(value="/api/findUserid", method=RequestMethod.POST)
	public Map<String, Object> findUserid(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call findUserid...!");
		logger.info("companyName : {}", reqParam.getCompanyName());
		logger.info("companyNo : {}", reqParam.getCompanyNo());
		logger.info("email : {}", reqParam.getEmail());
		logger.info("**********************************************************");
		
		//Map<String, Object> map = new HashMap<String, Object>();
		
		int isOk = 1;
		//Date currentTime = Calendar.getInstance().getTime();
		//long epochTime = currentTime.getTime();
		
		// 유효성 체크
		if (StringUtils.isEmpty(reqParam.getCompanyName())
				|| StringUtils.isEmpty(reqParam.getCompanyNo())
				|| StringUtils.isEmpty(reqParam.getEmail())) {
			
			isOk = 0;
		}
		if ( reqParam.getCompanyName().length() > 12 ) {
			logger.error("[findUserid] Error => companyName Length [len>12] : {}", reqParam.getCompanyName().length());
			isOk = -1;
		}
		if ( !Pattern.matches("^[a-zA-Z0-9가-힣()㈜ ]*$", reqParam.getCompanyName()) ) {
			logger.error("[findUserid] Error => companyName Pattern Matches [^[a-zA-Z0-9가-힣()㈜ ]*$] : {}", Pattern.matches("^[a-zA-Z0-9가-힣()㈜ ]*$", reqParam.getCompanyName()));
			isOk = -1;
		}
		if ( reqParam.getCompanyNo().replaceAll("-", "").length() == 10 ) {
			if ( !Pattern.matches("^[0-9]*$", reqParam.getCompanyNo().replaceAll("-", "")) ) {
				logger.error("[findUserid] Error => companyNo Pattern Matches [^[0-9]*$] : {}", Pattern.matches("^[0-9]*$", reqParam.getCompanyNo().replaceAll("-", "")));
				isOk = -1;
			}
		} else {
			logger.error("[findUserid] Error => companyNo Length [len==10] : {}", reqParam.getCompanyNo().replaceAll("-", "").length());
			isOk = -1;
		}
		if ( !StringUtils.isEmpty(reqParam.getEmail()) ) {
			if ( !Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", reqParam.getEmail()) ) {
				logger.error("[findUserid] Error => email Pattern Matches [^[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*\\\\.[a-zA-Z]{2,6}$] : {}", Pattern.matches("^[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*\\\\.[a-zA-Z]{2,6}$", reqParam.getEmail()));
				isOk = -1;
			}
		}
		
		if (isOk == 0) {
			logger.error("[findUserid] code : USER_0001_CODE");
			logger.error("[findUserid] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		if (isOk == -1) {
			logger.error("[findUserid] code : USER_0002_CODE");
			logger.error("[findUserid] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		
		// 아이디 찾기
		Map<String, Object> findIdMap = userService.findUserId(reqParam);
		logger.info("[findUserid] findUserId : {}", findIdMap.get("success").toString());
		
		// 찾는 아이디가 있다면 
		if ("true".equals(findIdMap.get("success").toString())) {
			// 랜덤 인증번호 생성
//			Random rnd = new Random();
//			StringBuffer buf = new StringBuffer();
//			for (int i=0, l=10; i<l; i++) {
//
//			    // rnd.nextBoolean()는 랜덤으로 true or false를 리턴. true라면 랜덤 소문자, false라면 랜덤 숫자를 StringBuffer에 append
//			    if (rnd.nextBoolean()) {
//			        buf.append((char) ((int) (rnd.nextInt(26)) + 97));
//			    } else {
//			        buf.append((rnd.nextInt(10)));
//			    }
//			}
			
			// 인증번호 생성
			String authNum = CommonUtils.numberGen(6, 1);
			
			// 메일로 인증번호 발송
			CommonVO mailParam = new CommonVO();
			mailParam.setEmail(reqParam.getEmail());
			mailParam.setEmailTitle("[아이봇] 아이디찾기 인증번호 발송 메일 입니다.");
			mailParam.setEmailText(authNum);
			mailParam.setSearchType("아이디");
			Map<String, Object> sendMailMap = sendMail(mailParam);
			
			logger.info("[findUserid] sendMail : {}", sendMailMap.get("success").toString());
			if ("true".equals(sendMailMap.get("success").toString())) {
				// 이메일 인증번호 업데이트
				reqParam.setAuthNumber(authNum);
				Map<String, Object> authNumMap = userService.updateTbUserAuthNumber(reqParam);
				return authNumMap;
			} else {
				return sendMailMap;
			}
		} else {
			return findIdMap;
		}
	}
	
	/**
	 * 메일 발송
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "메일발송", notes = "필수값 : email, emailText" )
	@RequestMapping(value="/api/sendMail", method=RequestMethod.POST)
	public Map<String, Object> sendMail(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call sendMail...!");
		logger.info("email : {}", reqParam.getEmail());
		logger.info("emailText : {}", reqParam.getEmailText());
		logger.info("**********************************************************");
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 메일 발송
		boolean isSend = mailService.sendMessageUsingTemplate(reqParam.getEmail(), reqParam.getEmailTitle(), reqParam.getEmailText(), reqParam.getSearchType());
		if (isSend) {
			logger.info("[sendMail] code : USER_0000_CODE");
			logger.info("[sendMail] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
			map.put("success", true);
			map.replace("error", codeComponent.getCodeMsg("USER_0000_CODE"));
		} else {
			logger.error("[sendMail] code : USER_0005_CODE");
			logger.error("[sendMail] msg : {}", codeComponent.getCodeMsg("USER_0005_CODE"));
			map.put("success", false);
			map.replace("error", codeComponent.getCodeMsg("USER_0005_CODE"));
		}
		
		return map;
	}
	
	/**
	 * 비밀번호 찾기 API
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "비밀번호 찾기", notes = "필수값 : userId, email" )
	@RequestMapping(value="/api/findUserpw", method=RequestMethod.POST)
	public Map<String, Object> findUserpw(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call findUserpw...!");
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("email : {}", reqParam.getEmail());
		logger.info("**********************************************************");
		
		Map<String, Object> map = new HashMap<String, Object>();
		//JSONObject dataJObj = null;
		
		int isOk = 1;
		Date currentTime = Calendar.getInstance().getTime();
		long epochTime = currentTime.getTime();
		
		// 유효성 체크
		if ( StringUtils.isEmpty(reqParam.getUserId()) || StringUtils.isEmpty(reqParam.getEmail()) ) {
			isOk = 0;
		}
		if ( reqParam.getUserId().length() < 6 || reqParam.getUserId().length() > 14 ) {
			logger.error("[findUserpw] Error => userId Length [len>14] : {}", reqParam.getUserId().length());
			isOk = -1;
		}
		if ( !Pattern.matches("^[a-z0-9]*$", reqParam.getUserId()) ) {
			logger.error("[findUserpw] Error => userId Pattern Matches [^[a-z0-9]*$] : {}", Pattern.matches("^[a-z0-9]*$", reqParam.getUserId()));
			isOk = -1;
		}
		if ( !StringUtils.isEmpty(reqParam.getEmail()) ) {
			if ( !Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", reqParam.getEmail()) ) {
				logger.error("[findUserpw] Error => email Pattern Matches [^[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\\\.]?[0-9a-zA-Z])*\\\\.[a-zA-Z]{2,6}$] : {}", Pattern.matches("^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,6}$", reqParam.getEmail()));
				isOk = -1;
			}
		}
		
		if (isOk == 0) {
			logger.info("[findUserpw] code : USER_0001_CODE");
			logger.info("[findUserpw] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		if (isOk == -1) {
			logger.error("[findUserpw] code : USER_0002_CODE");
			logger.error("[findUserpw] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("success", false);
			map.put("error", codeComponent.getCodeMsg("USER_0002_CODE"));
			map.put("accessTime", epochTime);
			map.put("data", null);
			return map;
		}
		
		// 비밀번호 찾기
		Map<String, Object> findPwMap = userService.findUserPw(reqParam);
		logger.info("[findUserpw] findPw : {}", findPwMap.get("success").toString());
		
		// 찾는 아이디가 있다면 
		if ("true".equals(findPwMap.get("success").toString())) {
			// 랜덤 인증번호 생성
//					Random rnd = new Random();
//					StringBuffer buf = new StringBuffer();
//					for (int i=0, l=10; i<l; i++) {
//					    // rnd.nextBoolean()는 랜덤으로 true or false를 리턴. true라면 랜덤 소문자, false라면 랜덤 숫자를 StringBuffer에 append
//					    if (rnd.nextBoolean()) {
//					        buf.append((char) ((int) (rnd.nextInt(26)) + 97));
//					    } else {
//					        buf.append((rnd.nextInt(10)));
//					    }
//					}
			
			// 인증번호 생성
			String authNum = CommonUtils.numberGen(6, 1);
			
			// 메일로 인증번호 발송
			CommonVO mailParam = new CommonVO();
			mailParam.setEmail(reqParam.getEmail());
			mailParam.setEmailTitle("[아이봇] 비밀번호찾기 인증번호 발송 메일 입니다.");
			mailParam.setEmailText(authNum);
			mailParam.setSearchType("비밀번호");
			Map<String, Object> sendMailMap = sendMail(mailParam);
			logger.info("[findUserpw] sendMail : {}", sendMailMap.get("success").toString());
			
			if ("true".equals(sendMailMap.get("success").toString())) {
				// 이메일 인증번호 업데이트
				reqParam.setAuthNumber(authNum);
				Map<String, Object> authNumMap = userService.updateTbUserAuthNumber(reqParam);
				return authNumMap;
			} else {
				return sendMailMap;
			}
		} else {
			return findPwMap;
		}
	}
	
	/**
	 * 아이디찾기 비밀번호찾기 인증번호 확인
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "인증번호 확인", notes = "필수값 : 아이디 찾기인 경우 companyName, companyNo, email, authNumber / 비밀번호 찾기인 경우 userId, email, authNumber" )
	@RequestMapping(value="/api/checkAuthNum", method=RequestMethod.POST)
	public Map<String, Object> checkAuthNum(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call checkAuthNum...!");
		logger.info("companyName : {}", reqParam.getCompanyName());
		logger.info("companyNo : {}", reqParam.getCompanyNo());
		logger.info("email : {}", reqParam.getEmail());
		logger.info("userId : {}", reqParam.getUserId());
		logger.info("authNumber : {}", reqParam.getAuthNumber());
		logger.info("**********************************************************");
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 아이디찾기 비밀번호찾기 인증번호 확인
		map = userService.checkAuthNum(reqParam);
		
		return map;
	}
	
	/**
	 * 비밀번호찾기 비밀번호 재설정
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "비밀번호찾기 비밀번호 재설정", notes = "필수값 : userId, userPass, authNumber(인증번호)" )
	@RequestMapping(value="/api/changeUserPw", method=RequestMethod.POST)
	public Map<String, Object> changeUserPw(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("Call changeUserPw...!");
		logger.info("userId : {}", reqParam.getUserId());
		//logger.info("userPass : {}", reqParam.getUserPass());
		logger.info("authNumber : {}", reqParam.getAuthNumber());
		logger.info("**********************************************************");
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 비밀번호찾기 비밀번호 재설정
		map = userService.changeUserPw(reqParam);
		
		return map;
	}
	
	/**
	 * 사용자 추가 API
	 * @param user
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "사용자 추가", produces = "application/json", response = User.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "username", value="사용자 이름(아이디)", required = true, dataType = "string", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "password", value="사용자 패스워드", required = true, dataType = "string", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "user_type", value="사용자 타입", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "사용자 이름이 없습니다.|사용자 패스워드가 없습니다.|사용자 타입이 없습니다.|이미 등록된 사용자 이름입니다."),
	})
	@PostMapping("/api/user/add")
	public User userAdd(@RequestBody User user) throws Exception {

		return userService.add(user);
	}

	/**
	 * 사용자 화면 비밀번호 초기화 API
	 * 1. 사용자 이름과 메일 체크
	 * 2. 비밀번호 리셋
	 * 3. 메일 발송
	 *
	 * @return
	 */
	@ApiOperation(value = "패스워드 초기화", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "user_name", value="사용자 이름(아이디)", required = true, dataType = "string", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "email", value="이메일", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "사용자 아이디가 없습니다.|사용자 이메일이 없습니다.|사용자를 찾을 수 없습니다.|이메일 주소가 다릅니다."),
	})
	@PostMapping("/api/pw_reset")
	public void resetPassword(@RequestParam("user_name") String userName, @RequestParam("email") String email) {

		userService.resetPassword(userName, email);
	}

	/**
	 * 관리자 화면 비밀번호 초기화 API
	 * 1. 사용자 ID와 메일 체크
	 * 2. 비밀번호 리셋
	 * 3. 메일 발송
	 *
	 * @return
	 */
	@ApiOperation(value = "패스워드 초기화",
			produces = "application/json",
			authorizations = {
			@Authorization(
					value = "",
					scopes = {
							@AuthorizationScope(
									scope = "homepage,write",
									description = "")
					}
			)
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "user_id", value="사용자 시퀀스", required = true, dataType = "long", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "email", value="이메일", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "사용자 아이디가 없습니다.|사용자 이메일이 없습니다.|사용자를 찾을 수 없습니다.|이메일 주소가 다릅니다."),
	})
	@PostMapping("/api/auth/pw_reset")
	@PreAuthorize("#oauth2.hasScope('homepage') and #oauth2.hasScope('write')")
	public void resetPassword(@RequestParam("user_id") long userID, @RequestParam("email") String email) {

		userService.resetPassword(userID, email);
	}

	/**
	 * 비밀번호 변경
	 *
	 * @return
	 */
	@ApiOperation(value = "패스워드 변경",
			produces = "application/json",
			authorizations = {
			@Authorization(
					value = "",
					scopes = {
							@AuthorizationScope(
									scope = "homepage,write",
									description = "")
					}
			)
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "user_name", value="사용자 이름(아이디)", required = true, dataType = "string", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "old_password", value="현재 패스워드", required = true, dataType = "string", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "new_password", value="신규 패스워드", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "사용자 아이디가 없습니다.|현재 패스워드가 없습니다.|신규 패스워드가 없습니다.|사용자를 찾을 수 없습니다.|현재 패스워드가 잘못되었습니다."),
	})
	@PostMapping("/api/auth/pw_change")
	@PreAuthorize("#oauth2.hasScope('homepage') and #oauth2.hasScope('write')")
	public AuthError changePassword(@RequestParam("user_name") String userName, @RequestParam("old_password") String oldPassword, @RequestParam("new_password") String newPassword) {

		AuthError authError = userService.passwordChange(userName, oldPassword, newPassword);

		return authError;
	}

	/**
	 * 사용자 중복 체크
	 * @param userName
	 */
	@ApiOperation(value = "사용자 중복 체크", produces = "application/json")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "user_name", value="사용자 이름(아이디)", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "사용자 이름이 없습니다.|이미 존재하는 사용자입니다."),
	})
	@PostMapping("/api/user/duplicate_username")
	public void duplicateUserName(@RequestParam("user_name") String userName) {
		
		userService.duplicateUserName(userName);
		
	}

	/**
	 * 캐쉬 업데이트
	 *
	 * @param mallID
	 * @param cash
	 */
	@ApiOperation(value = "캐쉬 업데이트",
			produces = "application/json",
			authorizations = {
					@Authorization(
							value = "",
							scopes = {
									@AuthorizationScope(
											scope = "homepage,write",
											description = "")
							}
					)
			})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "mall_id", value="몰 ID", required = true, dataType = "long", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "cash", value="추가 캐쉬", required = true, dataType = "int", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "payment_type", value="결제 방식(CREDIT|BANKBOOK|FREE)", required = true, dataType = "string", paramType = "query", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "몰 ID가 없습니다.|캐쉬 값이 잘못되었습니다."),
	})
	@PreAuthorize("#oauth2.hasScope('homepage') and #oauth2.hasScope('write')")
	@PostMapping("/api/auth/cash_update")
	public void serviceCashUpdate(@RequestParam("mall_id") long mallID,
	                              @RequestParam("cash") int cash,
	                              @RequestParam("payment_type")PaymentType paymentType) {

		if(paymentType == PaymentType.FREE)
			mallService.addFreeCash(mallID, cash, false, true);
		else
			mallService.addCash(mallID, cash, paymentType);
	}

	/**
	 * 무료 서비스 기간 업데이트
	 *
	 * @param mallID
	 * @param endDate
	 */
	@ApiOperation(value = "무료 서비스 기간 업데이트",
			produces = "application/json",
			authorizations = {
					@Authorization(
							value = "",
							scopes = {
									@AuthorizationScope(
											scope = "homepage,write",
											description = "")
							}
					)
			})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "mall_id", value="몰 ID", required = true, dataType = "long", paramType = "query", defaultValue = ""),
			@ApiImplicitParam(name = "end_date", value="무료 서비스 종료 날짜(yyyyMMdd)", required = true, dataType = "string", paramType = "query", defaultValue = "", format = "yyyyMMdd", example = "20181231"),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "몰 ID가 없습니다.|무료 서비스 기간 설정이 잘못되었습니다."),
	})
	@PreAuthorize("#oauth2.hasScope('homepage') and #oauth2.hasScope('write')")
	@PostMapping("/api/auth/date_update")
	public void serviceDateUpdate(@RequestParam("mall_id") long mallID, @RequestParam("end_date") @DateTimeFormat(pattern = "yyyyMMdd") Date endDate) {

		mallService.dateUpdate(mallID, endDate);
	}


	/**
	 * 몰 상품 정보
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "몰 상품 정보",
			produces = "application/json",
			authorizations = {
					@Authorization(
							value = "",
							scopes = {
									@AuthorizationScope(
											scope = "homepage,write",
											description = "")
							}
					)
			},
			response = ServiceInfo.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value="몰 ID", required = true, dataType = "long", paramType = "path", defaultValue = ""),
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "몰 ID가 없습니다."),
	})
	@PreAuthorize("#oauth2.hasScope('homepage') and #oauth2.hasScope('write')")
	@GetMapping("/api/auth/shop/{id}")
	public ServiceInfo shopInfo(@PathVariable final long id) {

		return mallService.getServiceInfo(id);
	}
	
	/**
	 * 로그인 토큰(access_token) 암호화 API (통합페이지에서 사용)
	 * 2021-12-02 shlee : RestUserController 정리 완료
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@CrossOrigin(origins="*")
	@ApiOperation( value = "로그인 토큰(access_token) 암호화", notes = "필수값 : user_login" )
	@RequestMapping(value="/api/getEncData", method=RequestMethod.POST)
	public ResultVO getEncData(@RequestBody TbUser reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][getEncData] userLoginId : {}", reqParam.getUser_login());
		logger.info("**********************************************************");
		
		Map<String, String> map = new HashMap<String, String>();
		ResultVO resultVO = new ResultVO( null, UserCode.USER_0000_CODE, codeComponent.getCodeMsg(UserCode.USER_0000_CODE) );
		String userLogin =  Optional.ofNullable(reqParam.getUser_login()).orElse("");
		
		if(userLogin.isEmpty()) {
			logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
			logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			return resultVO;
		}
		
		try {
			
			TbUser tbUser = userService.getTbUserInfo(userLogin);
			String accessToken = "";
			if(tbUser != null) {
				accessToken = Optional.ofNullable(tbUser.getAccess_token()).orElse("");
			}
			
			if(accessToken.isEmpty()) {
				logger.error("[RestApiController.java][outLinkUserSync] code : USER_0006_CODE");
				logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				resultVO.setResultCode(UserCode.USER_0006_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0006_CODE"));
				return resultVO;
			}
			// 암호화
			String encStr = restTokenService.aesEncrypt(userLogin, accessToken);
			map.put("enc", encStr);
			resultVO.setItems(map);
			
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][getEncData] code : USER_9999_CODE");
			logger.error("[RestApiController.java][getEncData] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
	/**
	 * 자동 로그인 연결 API (통합페이지에서 사용)
	 * @param encData
	 * @param redirectUrl
	 * @return
	 * @throws Exception
	 */
	@CrossOrigin(origins="*")
	@ApiOperation( value = "자동 로그인 연결 (bridge)", notes = "필수값 : endData \n 옵션값 : redirectUrl" )
	@RequestMapping(value="/api/bridge", method=RequestMethod.GET)
	public ResultVO bridge(
			@RequestParam(value="encData", defaultValue = "", required = true) String encData,
			@RequestParam(value="redirectUrl", defaultValue = "", required = false) String redirectUrl) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][bridge] encData : {}", encData);
		logger.info("[RestApiController.java][bridge] redirectUrl : {}", redirectUrl);
		logger.info("**********************************************************");
		
		String encDataStr = Optional.ofNullable(encData).orElse("").trim(); 			//필수
		String resultRedirectUrl = Optional.ofNullable(redirectUrl).orElse("").trim(); 	//필수
		
		ResultVO resultVO = new ResultVO( null, UserCode.USER_0000_CODE, codeComponent.getCodeMsg(UserCode.USER_0000_CODE) );
		
		//
		if ( StringUtils.isEmpty(encDataStr) ) {
			if(encData.trim().isEmpty()) {
				logger.error("[RestApiController.java][bridge] code : USER_0001_CODE");
				logger.error("[RestApiController.java][bridge] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				resultVO.setResultCode(UserCode.USER_0001_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
				return resultVO;
			}
		}
		
		try {
			
			// 복호화
			JSONObject resultJObj = restTokenService.aesDecrypt(encDataStr);
			// 로그인 정보 조회
			TbUser tbuser = userService.findByUserLoginToken(resultJObj.getAsString("user_id"), resultJObj.getAsString("access_token"));
			
			Map<String, String> resultMap = new HashMap<String, String>();
			
			if (tbuser != null && tbuser.getAccess_token() != null) {
				String token = tbuser.getAccess_token();
				resultMap.put("accessToken", token);
			} else {
				resultMap.put("accessToken", "");
				logger.error("[RestApiController.java][bridge] code : USER_0006_CODE");
				logger.error("[RestApiController.java][bridge] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				resultVO.setResultCode(UserCode.USER_0006_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0006_CODE"));
			}
			resultMap.put("redirectUrl", resultRedirectUrl);
			resultVO.setItems(resultMap);
			
		} catch(Exception e) {
			
			e.printStackTrace();
			logger.error("[RestApiController.java][bridge] code : USER_9999_CODE");
			logger.error("[RestApiController.java][bridge] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			resultVO.setItems(null);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			
		}
		return resultVO;
		
	}
	
	/**
	 * 동의 여부 수정 (아웃바이저 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "동의 여부 수정", notes = "필수값 : userId, agreement004" )
	@RequestMapping(value="/api/modifyUserInfoWithAgreement", method=RequestMethod.POST)
	public ResultVO modifyUserInfoWithAgreement(@RequestBody CommonVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][modifyUserInfoWithAgreement] userId : {}", reqParam.getUserId());
		logger.info("[RestApiController.java][modifyUserInfoWithAgreement] agreement004 : {}", reqParam.getAgreement004());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO( null, UserCode.USER_0000_CODE, codeComponent.getCodeMsg(UserCode.USER_0000_CODE) );
		
		String userId = Optional.ofNullable(reqParam.getUserId()).orElse("");
		String agreement004 = Optional.ofNullable(reqParam.getAgreement004()).orElse("");
		
		boolean agreementCheck = false;
		if("Y".equals(agreement004) || "N".equals(agreement004)) agreementCheck = true;
		
		if(userId.trim().isEmpty() || agreement004.trim().isEmpty() || !agreementCheck) {
			logger.error("[RestApiController.java][modifyUserInfoWithAgreement] code : USER_0001_CODE");
			logger.error("[RestApiController.java][modifyUserInfoWithAgreement] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			return resultVO;
		}
		
		try {
			
			TbUser resultTbuser = userService.selectTbUserInfo(userId);
			if(resultTbuser == null) {
				logger.error("[RestApiController.java][modifyUserInfoWithAgreement] code : USER_0006_CODE");
				logger.error("[RestApiController.java][modifyUserInfoWithAgreement] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				resultVO.setResultCode(UserCode.USER_0006_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0006_CODE"));
				return resultVO;
			}
			
			resultTbuser.setAgreement004(agreement004);
			int modifyCnt = userService.modifyUserInfoWithAgreement(resultTbuser);
			logger.info("[RestApiController.java][modifyUserInfoWithAgreement] modifyCnt : {}", modifyCnt);
			
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][modifyUserInfoWithAgreement] code : USER_9999_CODE");
			logger.error("[RestApiController.java][modifyUserInfoWithAgreement] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			resultVO.setItems(null);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
}