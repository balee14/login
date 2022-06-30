package com.chatbot.adjustment.web.controller.rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.adjustment.service.RestExtemalService;
import com.chatbot.adjustment.service.RestUserService;
import com.chatbot.adjustment.web.common.component.ResultCodeComponent;
import com.chatbot.adjustment.web.common.util.CommonUtils;
import com.chatbot.adjustment.web.common.util.DateUtils;
import com.chatbot.adjustment.web.common.util.RestAPIUtil;
import com.chatbot.adjustment.web.common.util.XSSFilter;
import com.chatbot.adjustment.web.common.vo.OutLinkReqVO;
import com.chatbot.adjustment.web.common.vo.ResultVO;
import com.chatbot.adjustment.web.common.vo.UserCode;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.ApiOperation;

@RestController
public class RestExtemalController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${viser.api.terminate-user-url:'http://192.168.150.8:8080/api/account/terminationLink'}")
	private String VISER_API_TERMINATE_USER_URL;
	
	//JWT 토큰 만들 때 필요한 key 값 (로그인 소스내에도 존재해서 동일하게 맞춤)
	private String TOKEN_SECRET_KEY = "TNDtMCEn_TNSTvba7T16E1pK9DxcMa1pMpjJ9IYkmeTHGtcqnkIQ9WmiqeYQeIn6kyfs-UIGiw1hRqdv3BHa5mUqVn0536wDcJFB_niO6evPjt4mi5veMmx4wvGKYvQu3v2_zn0rNz43srWJYWZ7yJrSb_Dsgaw7SIF9fukvV7RQlI-kAdBnyo0y9KMNT4eZJJ39c-cJK9pYzzN7lYSBzLfqdolCiC6qpafIVowJlcQlrhLCtNdIQuSZ6cYBSjDhKTT-R07TG1ZmQRCeyP-7ummKoV1zMeKJGRziAng50U1qjEVSEZFn9d_ThlZj02G-on0VYAxZzs0XzXV8wN7I1g";
	
	private Date currentTime = Calendar.getInstance().getTime();
	private long epochTime = currentTime.getTime();
	
	@Autowired
	private ResultCodeComponent codeComponent;
	
	@Autowired
	private RestAPIUtil restAPIUtil;
	
	@Autowired
    private RestExtemalService restExtemalService;
	
	@Autowired
	private RestUserService restUserService;
	
	/**
	 * TODO 유효성체크 : 우선은 임시적으로 하드코딩-추후에 ENUM 제작
	 * 회원연동 API (모비온 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원연동", notes = "필수값 : ibotId, ibotPw, adverId, serviceType" )
	@RequestMapping(value="/api/outLinkUserLogin", method=RequestMethod.POST)
	public ResultVO outLinkUserLogin(@RequestBody OutLinkReqVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][outLinkUserLogin] ibotId : {}", reqParam.getIbotId());
		logger.info("[RestApiController.java][outLinkUserLogin] ibotPw : {}", reqParam.getIbotPw());
		logger.info("[RestApiController.java][outLinkUserLogin] adverId : {}", reqParam.getAdverId());
		logger.info("[RestApiController.java][outLinkUserLogin] serviceType : {}", reqParam.getServiceType());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String currentDate = DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		
		String ibotId = Optional.ofNullable(reqParam.getIbotId()).orElse("");
		String ibotPw = Optional.ofNullable(reqParam.getIbotPw()).orElse("");
		String adverId = Optional.ofNullable(reqParam.getAdverId()).orElse("");
		String serviceType = Optional.ofNullable(reqParam.getServiceType()).orElse("");
		
		//공통값
		map.put("ibotId", ibotId);
		map.put("adverId", adverId);
		map.put("serviceType", serviceType);
		map.put("requestUrl", "/api/outLinkUserLogin");
		map.put("requestIp", CommonUtils.getIp());
		
		try {
			// 유효성 체크
			if ( StringUtils.isEmpty(ibotId.trim()) 
					|| StringUtils.isEmpty(ibotPw.trim()) 
					|| StringUtils.isEmpty(adverId.trim()) 
					|| StringUtils.isEmpty(serviceType.trim())
					|| !"MOBTUNE".equals(serviceType) ) {
				logger.error("[RestApiController.java][outLinkUserLogin] code : USER_0001_CODE");
				logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0001_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE"));
				
				return resultVO;
			}
			if (ibotId.length() != XSSFilter.XSS(ibotId).length()) {
				logger.error("[RestApiController.java][outLinkUserLogin] code : USER_0002_CODE");
				logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_0002_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0002_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0002_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0002_CODE, codeComponent.getCodeMsg("USER_0002_CODE"));
				
				return resultVO;
			}
			
			// tb_user에 아이봇 아이디가 있는지 조회
			Map<String, Object> duplChkMap = new HashMap<String, Object>();
			duplChkMap = restUserService.idDuplChk(ibotId, epochTime);
			
			if ( "true".equals(duplChkMap.get("success").toString()) ) {
				//아이봇아이디 없음 (아이봇에 아이디가 없는데 모비온에서 연동 요청들어온 경우)
				logger.error("[RestApiController.java][outLinkUserLogin] code : USER_0006_CODE");
				logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_0006_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0006_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0006_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0006_CODE, codeComponent.getCodeMsg("USER_0006_CODE"));
				
				return resultVO;
			}
			
			TbUserOutlink tbUserOutlinkData ;
			
			// tb_user에 유저가 있는지 조회(아이디/비번)
			TbUser resultUserInfo = restUserService.selectTbUserInfoWithPass(ibotId, ibotPw);
			
			if ( resultUserInfo != null ) {
				//아이봇 회원 맞음
				// 1) adverId 광고주 체크 (같은 아이디로 들어온 경우)
				TbUserOutlink tbUserOutlinkDup1 = restExtemalService.checkAdverIdDup(adverId, serviceType);
				if(tbUserOutlinkDup1 != null) {
					//광고주ID 중복체크
					logger.error("[RestApiController.java][outLinkUserLogin] code : USER_7000_CODE");
					logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_7000_CODE"));
					map.put("successYn", "N");
					map.put("responseDate", currentDate);
					
					resultVO.setItems(map);
					resultVO.setResultCode(UserCode.USER_7000_CODE);
					resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7000_CODE"));
					restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7000_CODE, codeComponent.getCodeMsg("USER_7000_CODE"));
					
					return resultVO;
				}
				
				// 2) adverId 광고주 체크 (다른 아이디로 들어온 경우)
				TbUserOutlink tbUserOutlinkDup2 = restExtemalService.checkIbotIdDup(ibotId, serviceType);
				if(tbUserOutlinkDup2 != null) {
					String adverIdOri = Optional.ofNullable(tbUserOutlinkDup2.getAdver_id()).orElse("");
					if(!adverIdOri.isEmpty() && adverIdOri != adverId) {
						//아이봇ID 중복체크
						logger.error("[RestApiController.java][outLinkUserLogin] code : USER_7000_CODE");
						logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_7000_CODE"));
						map.put("successYn", "N");
						map.put("responseDate", currentDate);
						
						resultVO.setItems(map);
						resultVO.setResultCode(UserCode.USER_7000_CODE);
						resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7000_CODE"));
						restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7000_CODE, codeComponent.getCodeMsg("USER_7000_CODE"));
						
						return resultVO;
					} 
				}
				
				TbUserOutlink tbUserOutlink = restExtemalService.getTbUserOutlink(ibotId, adverId, serviceType);
				if(tbUserOutlink != null) {
					//이미 연동된 정보가 있으면
					logger.error("[RestApiController.java][outLinkUserLogin] code : USER_7000_CODE");
					logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_7000_CODE"));
					map.put("successYn", "N");
					map.put("responseDate", currentDate);
					
					resultVO.setItems(map);
					resultVO.setResultCode(UserCode.USER_7000_CODE);
					resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7000_CODE"));
					restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7000_CODE, codeComponent.getCodeMsg("USER_7000_CODE"));
					
					return resultVO;
				} else {
					//연동 성공
					map.put("successYn", "Y");
					map.put("responseDate", currentDate);
					map.put("agreement001", "Y");
					//1:1 매핑
					tbUserOutlinkData = restExtemalService.mergeTbUserOutlink(ibotId, adverId, serviceType); //insert
					
					resultVO.setItems(map);
					resultVO.setResultCode(UserCode.USER_0000_CODE);
					resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0000_CODE"));
					restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0000_CODE, codeComponent.getCodeMsg("USER_0000_CODE"));
				}
			} else {
				//비번이 틀린 경우
				logger.error("[RestApiController.java][outLinkUserLogin] code : USER_0003_CODE");
				logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_0003_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0003_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0003_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0003_CODE, codeComponent.getCodeMsg("USER_0003_CODE"));
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][outLinkUserLogin] code : USER_9999_CODE");
			logger.error("[RestApiController.java][outLinkUserLogin] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	
	}
	
	/**
	 * 회원연동해제 API (모비온 CRM 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원연동해제", notes = "필수값 : ibotId, adverId, serviceType" )
	@RequestMapping(value="/api/outLinkUserLogout", method=RequestMethod.POST)
	public ResultVO outLinkUserLogout(@RequestBody OutLinkReqVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][outLinkUserLogout] ibotId : {}", reqParam.getIbotId());
		logger.info("[RestApiController.java][outLinkUserLogout] adverId : {}", reqParam.getAdverId());
		logger.info("[RestApiController.java][outLinkUserLogout] serviceType : {}", reqParam.getServiceType());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO();
		Map<String, Object> map = new HashMap<String, Object>();

		String currentDate = DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		
		String ibotId = Optional.ofNullable(reqParam.getIbotId()).orElse("");
		String adverId = Optional.ofNullable(reqParam.getAdverId()).orElse("");
		String serviceType = Optional.ofNullable(reqParam.getServiceType()).orElse("");
		
		//공통값
		map.put("ibotId", ibotId);
		map.put("adverId", adverId);
		map.put("serviceType", serviceType);
		map.put("requestUrl", "/api/outLinkUserLogout");
		map.put("requestIp", CommonUtils.getIp());
		
		// 유효성 체크
		if ( StringUtils.isEmpty(ibotId.trim())
				|| StringUtils.isEmpty(adverId.trim()) 
				|| StringUtils.isEmpty(serviceType.trim())
				|| !"MOBTUNE".equals(serviceType) ) {
			logger.error("[RestApiController.java][outLinkUserLogout] code : USER_0001_CODE");
			logger.error("[RestApiController.java][outLinkUserLogout] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE"));
			
			return resultVO;
		}
		
		try {
			
			TbUserOutlink tbUserOutlink = restExtemalService.getTbUserOutlink(ibotId, adverId, serviceType); //update
			
			if(tbUserOutlink != null) {
				map.put("successYn", "Y");
				map.put("responseDate", currentDate);
				
				//아웃바이저 연동 계정 해지 API 호출 start
				String token = Optional.ofNullable(tbUserOutlink.getToken()).orElse("");
				String rsCode = "";
				String rsMsg = "";
				Map<String, String> valueMap = new HashMap<String, String>();
				if(token.isEmpty()) {
					//토큰이 없는 경우 - 토큰 생성해서 아웃바이저팀에 호출 (토큰 없는 경우에는 아이봇에서 회원연동한 경우임)
					String reToken = restExtemalService.createOutLinkToken(tbUserOutlink.getUser_login(), tbUserOutlink.getAdver_id());
					valueMap.put("token", reToken);
				} else {
					//토큰이 있는 경우
					valueMap.put("token", token);
				}
				
				org.jose4j.json.internal.json_simple.JSONObject json = restAPIUtil.callOutViserTerminationLink(VISER_API_TERMINATE_USER_URL, valueMap);
				if( json != null && json.containsKey("rsCode") && json.containsKey("rsMsg") ) {
					rsCode = (String) Optional.ofNullable(json.get("rsCode")).orElse("");
					rsMsg = (String) Optional.ofNullable(json.get("rsMsg")).orElse("");
				}
				logger.info("[RestApiController.java][callOutViserTerminationLink] code : {}", rsCode);
				logger.info("[RestApiController.java][callOutViserTerminationLink] msg : {}", rsMsg);
				//아웃바이저 연동 계정 해지 API 호출 end
				
				tbUserOutlink.setToken("");
				tbUserOutlink.setAdver_id("");
				tbUserOutlink.setUpdated_dt(new Date());
				tbUserOutlink.setExpired_dt(null);
				restExtemalService.updateTbUserOutlink(tbUserOutlink);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0000_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0000_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0000_CODE, codeComponent.getCodeMsg("USER_0000_CODE")); 
			} else {
				logger.error("[RestApiController.java][outLinkUserLogout] code : USER_7001_CODE");
				logger.error("[RestApiController.java][outLinkUserLogout] msg : {}", codeComponent.getCodeMsg("USER_7001_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_7001_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7001_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7001_CODE, codeComponent.getCodeMsg("USER_7001_CODE")); 
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][outLinkUserLogout] code : USER_9999_CODE");
			logger.error("[RestApiController.java][outLinkUserLogout] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
	/**
	 * 토큰발행 API (모비온 CRM 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "토큰발행", notes = "필수값 : adverId, serviceType" )
	@RequestMapping(value="/api/getToken", method=RequestMethod.POST)
	public ResultVO getToken(@RequestBody OutLinkReqVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][getToken] adverId : {}", reqParam.getAdverId());
		logger.info("[RestApiController.java][getToken] serviceType : {}", reqParam.getServiceType());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO();
		//Map<String, Object> map = new HashMap<String, Object>();

		String currentDate = DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		
		String adverId = Optional.ofNullable(reqParam.getAdverId()).orElse("");
		String serviceType = Optional.ofNullable(reqParam.getServiceType()).orElse("");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ibotId", "");
		map.put("adverId", adverId);
		map.put("serviceType", serviceType);
		map.put("requestUrl", "/api/getToken");
		map.put("requestIp", CommonUtils.getIp());
		
		// 유효성 체크
		if ( StringUtils.isEmpty(adverId.trim()) 
				|| StringUtils.isEmpty(serviceType.trim()) 
				|| !"MOBTUNE".equals(serviceType) ) {
			logger.error("[RestApiController.java][getToken] code : USER_0001_CODE");
			logger.error("[RestApiController.java][getToken] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE"));
			
			return resultVO;
		}
		
		try {
			
			TbUserOutlink tbUserOutlink = restExtemalService.getTbUserOutlink(adverId, serviceType); //update
			
			if(tbUserOutlink != null) {
				map.put("successYn", "Y");
				map.put("responseDate", currentDate);
				
				String token = "";
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 30); //토큰 유지 기간:30일(한달)
				Date ontMonthAfterDate = c.getTime();
				
				try {
			        //Long expiredTime = 1000 * 60L * 60L * 2L; // 토큰 유효 시간 (2시간)
			        //Date ext = new Date(); // 토큰 만료 시간
			        //ext.setTime(ext.getTime() + expiredTime);
					
					token = restExtemalService.createOutLinkToken(tbUserOutlink.getUser_login(), tbUserOutlink.getAdver_id());

				} catch(Exception e) {
					e.printStackTrace();
					logger.error("[RestApiController.java][getToken] exception : {}", e.getMessage());
					logger.error("[RestApiController.java][getToken] code : USER_9999_CODE");
					logger.error("[RestApiController.java][getToken] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
					map.put("successYn", "N");
					map.put("responseDate", currentDate);
					
					resultVO.setItems(map);
					resultVO.setResultCode(UserCode.USER_9999_CODE);
					resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
					restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
					
					return resultVO;
				}
				
				tbUserOutlink.setUpdated_dt(new Date());
				tbUserOutlink.setToken(token); 				//user_login, adver_id, expired_dt 정보 암호화
				tbUserOutlink.setExpired_dt(ontMonthAfterDate); //호출한 시점부터 30일 (한달)
				restExtemalService.updateTbUserOutlink(tbUserOutlink);
				
				//responseDate(currentDate)와 동일하게 형식 전달
				SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String expriedDate = fromFormat.format(ontMonthAfterDate);
				map.put("ibotId", tbUserOutlink.getUser_login());
				map.put("token", token);
				map.put("expriedDate", expriedDate); 		//응답 view용 (패턴 : yyyy-MM-dd HH:mm:ss)
				map.put("expried_date", ontMonthAfterDate); //db 저장용
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_0000_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0000_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0000_CODE, codeComponent.getCodeMsg("USER_0000_CODE")); 
			} else {
				logger.error("[RestApiController.java][getToken] code : USER_7001_CODE");
				logger.error("[RestApiController.java][getToken] msg : {}", codeComponent.getCodeMsg("USER_7001_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_7001_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7001_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7001_CODE, codeComponent.getCodeMsg("USER_7001_CODE")); 
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][getToken] code : USER_9999_CODE");
			logger.error("[RestApiController.java][getToken] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
	/**
	 * 토큰 유효성 체크 API (아웃바이저 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "토큰 유효성 체크 (회원연동 토큰 체크)", notes = "필수값 : token, serviceType" )
	@RequestMapping(value="/api/validCheckToken", method=RequestMethod.POST)
	public ResultVO validCheckToken(@RequestBody OutLinkReqVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][validCheckToken] token : {}", reqParam.getToken());
		logger.info("[RestApiController.java][validCheckToken] serviceType : {}", reqParam.getServiceType());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO();
		Map<String, Object> map = new HashMap<String, Object>();

		String currentDate = DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		
		String token = Optional.ofNullable(reqParam.getToken()).orElse("");
		String serviceType = Optional.ofNullable(reqParam.getServiceType()).orElse("");
		
		//공통값
		map.put("ibotId", "");
		map.put("adverId", "");
		map.put("serviceType", serviceType);
		map.put("token", token);
		map.put("requestUrl", "/api/validCheckToken");
		map.put("requestIp", CommonUtils.getIp());
		
		// 유효성 체크
		if ( StringUtils.isEmpty(token.trim()) 
				|| StringUtils.isEmpty(serviceType.trim())
				|| !"MOBTUNE".equals(serviceType) ) {
			logger.error("[RestApiController.java][validCheckToken] code : USER_0001_CODE");
			logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE"));
			
			return resultVO;
		}
		
		try {
			
			//token값 복호화
			//case1.AES256 복호화
			/*
			Aes256 aes = new Aes256();
			aes.setKey(LOGIN_TOKEN_KEY);
			
			String decStr = aes.decrypt(token); //복호화
			
			JSONParser jparser = new JSONParser();
			JSONObject resultJObj = (JSONObject) jparser.parse(decStr);
			
			boolean ibotIdCheck = resultJObj.containsKey("user_login");
			boolean adverIdCheck = resultJObj.containsKey("adver_id");
			boolean expiredDateCheck = resultJObj.containsKey("expired_dt");
			*/
			//case2.JWT 검증(유효검사체크)
			Map<String, Object> claimMap = null;
			String ibotId = "";
			String adverId = "";
			long expiredDate = 0; //unix timestamp (10자리)
			
            Claims claims = Jwts.parser()
                    .setSigningKey(TOKEN_SECRET_KEY.getBytes("UTF-8")) // Set Key
                    .parseClaimsJws(token) // 파싱 및 검증, 실패 시 에러
                    .getBody();

            claimMap = claims;
            
    		boolean ibotIdCheck = claims.containsKey("user_login");
			boolean adverIdCheck = claims.containsKey("adver_id");
			boolean expiredDateCheck = claims.containsKey("expired_dt");

            //Date expiration = claims.get("exp", Date.class);
			
            if( ibotIdCheck && adverIdCheck && expiredDateCheck ) {
				ibotId = Optional.ofNullable(claims.get("user_login", String.class)).orElse("");
				adverId = Optional.ofNullable(claims.get("adver_id", String.class)).orElse("");
				expiredDate = Optional.ofNullable(claims.get("expired_dt", Long.class)).orElse((long) 0);
				
				if( ibotId.isEmpty() || adverId.isEmpty() || expiredDate == 0 ) {
					//실패:필수값 누락
					logger.error("[RestApiController.java][validCheckToken] code : USER_0001_CODE");
					logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
					map.put("successYn", "N");
					map.put("responseDate", currentDate);
					
					resultVO.setItems(map);
					resultVO.setResultCode(UserCode.USER_0001_CODE);
					resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
					restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE")); 
				} else {
					//유저정보 체크
					TbUserOutlink tbUserOutlink = restExtemalService.getTbUserOutlink(ibotId, adverId, serviceType);
					if(tbUserOutlink == null) {
						//실패:유저정보 미존재
						logger.error("[RestApiController.java][validCheckToken] code : USER_7001_CODE");
						logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_7001_CODE"));
						map.put("successYn", "N");
						map.put("responseDate", currentDate);
						
						resultVO.setItems(map);
						resultVO.setResultCode(UserCode.USER_7001_CODE);
						resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7001_CODE"));
						restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7001_CODE, codeComponent.getCodeMsg("USER_7001_CODE")); 
					} else {
						//유효날짜 체크
						Date expriedDateData = new Date(expiredDate*1000);
						//현재 날짜와 비교하여 유효한지 체크
						int compare = expriedDateData.compareTo(new Date());
						if(compare < 0) {
							//실패:유효날짜(만료날짜)가 과거이면은 토크이 유효하지 않아 재발급 필요
							logger.error("[RestApiController.java][validCheckToken] code : USER_7003_CODE");
							logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_7003_CODE"));
							map.put("successYn", "N");
							map.put("responseDate", currentDate);
							
							resultVO.setItems(map);
							resultVO.setResultCode(UserCode.USER_7003_CODE);
							resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7003_CODE"));
							restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7003_CODE, codeComponent.getCodeMsg("USER_7003_CODE")); 
						} else {
							//성공:아이디 2개 반환, 유효날짜 반환
							logger.error("[RestApiController.java][validCheckToken] code : USER_0000_CODE");
							logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_0000_CODE"));
							map.put("ibotId", ibotId);
							map.put("adverId", adverId);
							map.put("successYn", "Y");
							map.put("responseDate", currentDate);
							SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String expriedDate = fromFormat.format(expriedDateData);
							map.put("expriedDate", expriedDate);
							
							resultVO.setItems(map);
							resultVO.setResultCode(UserCode.USER_0000_CODE);
							resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0000_CODE"));
							restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0000_CODE, codeComponent.getCodeMsg("USER_0000_CODE")); 
						}
					}
				}
			} else {
				//실패:토큰 문법 파싱 에러
				logger.error("[RestApiController.java][validCheckToken] code : USER_7002_CODE");
				logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_7002_CODE"));
				map.put("successYn", "N");
				map.put("responseDate", currentDate);
				
				resultVO.setItems(map);
				resultVO.setResultCode(UserCode.USER_7002_CODE);
				resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7002_CODE"));
				restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7002_CODE, codeComponent.getCodeMsg("USER_7002_CODE")); 
			}
		} catch (ExpiredJwtException e) { // 토큰이 만료되었을 경우
			logger.error("[RestApiController.java][validCheckToken] code : USER_7003_CODE");
			logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_7003_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_7003_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_7003_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_7003_CODE, codeComponent.getCodeMsg("USER_7003_CODE")); 
		} catch(Exception e) {
			//실패:시스템 에러
			e.printStackTrace();
			logger.error("[RestApiController.java][validCheckToken] code : USER_9999_CODE");
			logger.error("[RestApiController.java][validCheckToken] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
	/**
	 * 회원연동싱크 API (통합페이지에서 사용)
	 * @param reqParam
	 * @return
	 * @throws Exception
	 */
	@ApiOperation( value = "회원연동싱크", notes = "필수값 : ibotId, adverId, serviceType, linkYn" )
	@RequestMapping(value="/api/outLinkUserSync", method=RequestMethod.POST)
	public ResultVO outLinkUserSync(@RequestBody OutLinkReqVO reqParam) throws Exception {
		
		logger.info("**********************************************************");
		logger.info("[RestApiController.java][outLinkUserSync] ibotId : {}", reqParam.getIbotId());
		logger.info("[RestApiController.java][outLinkUserSync] adverId : {}", reqParam.getAdverId());
		logger.info("[RestApiController.java][outLinkUserSync] serviceType : {}", reqParam.getServiceType());
		logger.info("[RestApiController.java][outLinkUserSync] linkYn : {}", reqParam.getLinkYn());
		logger.info("**********************************************************");
		
		ResultVO resultVO = new ResultVO();
		Map<String, Object> map = new HashMap<String, Object>();

		String currentDate = DateUtils.getCurrentDateTime("yyyy-MM-dd HH:mm:ss");
		
		String ibotId = Optional.ofNullable(reqParam.getIbotId()).orElse("");
		String adverId = Optional.ofNullable(reqParam.getAdverId()).orElse("");
		String serviceType = Optional.ofNullable(reqParam.getServiceType()).orElse("");
		String linkYn = Optional.ofNullable(reqParam.getLinkYn()).orElse("");
		
		//공통값
		map.put("ibotId", ibotId);
		map.put("adverId", adverId);
		map.put("serviceType", serviceType);
		map.put("requestUrl", "/api/outLinkUserSync");
		map.put("requestIp", CommonUtils.getIp());
		
		// 유효성 체크
		if ( StringUtils.isEmpty(ibotId.trim()) 
				|| StringUtils.isEmpty(adverId.trim()) 
				|| StringUtils.isEmpty(serviceType.trim()) 
				|| StringUtils.isEmpty(linkYn.trim()) 
				|| !"MOBTUNE".equals(serviceType) ) {
			logger.error("[RestApiController.java][outLinkUserSync] code : USER_0001_CODE");
			logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_0001_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_0001_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0001_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0001_CODE, codeComponent.getCodeMsg("USER_0001_CODE"));
			
			return resultVO;
		}
		
		try {
			//아이봇 로그인 성공 / 모비온 로그인 성공 한 경우이기에 서로의 아이디를 싱크를 맞춰줍니다.
			TbUserOutlink tbUserOutlink = restExtemalService.getTbUserOutlink(ibotId, adverId, serviceType); //update
			if("Y".equals(linkYn)) {
				//연동가동
				if(tbUserOutlink != null) {
					map.put("successYn", "Y");
					map.put("responseDate", currentDate);
					map.put("agreement001", "Y");
					
					tbUserOutlink.setUser_login(ibotId);
					tbUserOutlink.setAdver_id(adverId);
					tbUserOutlink.setUpdated_dt(new Date());
					tbUserOutlink.setAgreement001("Y");
					restExtemalService.updateTbUserOutlink(tbUserOutlink); 			  //update
				} else {
					//저장
					map.put("successYn", "Y");
					map.put("responseDate", currentDate);
					map.put("agreement001", "Y");
					
					restExtemalService.mergeTbUserOutlink(ibotId, adverId, serviceType); //insert
				}
			} else if("N".equals(linkYn)){
				//연동해제 (연동되어 있던 데이터들만 연동해제를 할 수 있습니다)
				if(tbUserOutlink != null) {
					map.put("successYn", "Y");
					map.put("responseDate", currentDate);
					
					//아웃바이저 연동 계정 해지 API 호출 start
					String token = Optional.ofNullable(tbUserOutlink.getToken()).orElse("");
					String rsCode = "";
					String rsMsg = "";
					Map<String, String> valueMap = new HashMap<String, String>();
					if(token.isEmpty()) {
						//토큰이 없는 경우 - 토큰 생성해서 아웃바이저팀에 호출 (토큰 없는 경우에는 아이봇에서 회원연동한 경우임)
						String reToken = restExtemalService.createOutLinkToken(tbUserOutlink.getUser_login(), tbUserOutlink.getAdver_id());
						valueMap.put("token", reToken);
					} else {
						//토큰이 있는 경우
						valueMap.put("token", token);
					}
					
					org.jose4j.json.internal.json_simple.JSONObject json = restAPIUtil.callOutViserTerminationLink(VISER_API_TERMINATE_USER_URL, valueMap);
					if( json != null && json.containsKey("rsCode") && json.containsKey("rsMsg") ) {
						rsCode = (String) Optional.ofNullable(json.get("rsCode")).orElse("");
						rsMsg = (String) Optional.ofNullable(json.get("rsMsg")).orElse("");
					}
					logger.info("[RestApiController.java][callOutViserTerminationLink] code : {}", rsCode);
					logger.info("[RestApiController.java][callOutViserTerminationLink] msg : {}", rsMsg);
					//아웃바이저 연동 계정 해지 API 호출 end
					
					tbUserOutlink.setToken("");
					tbUserOutlink.setAdver_id("");
					tbUserOutlink.setUpdated_dt(new Date());
					tbUserOutlink.setExpired_dt(null);
					
					restExtemalService.updateTbUserOutlink(tbUserOutlink); 			  //update
				}
			}
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_0000_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_0000_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_0000_CODE, codeComponent.getCodeMsg("USER_0000_CODE")); 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("[RestApiController.java][outLinkUserSync] code : USER_9999_CODE");
			logger.error("[RestApiController.java][outLinkUserSync] msg : {}", codeComponent.getCodeMsg("USER_9999_CODE"));
			map.put("successYn", "N");
			map.put("responseDate", currentDate);
			
			resultVO.setItems(map);
			resultVO.setResultCode(UserCode.USER_9999_CODE);
			resultVO.setResultMessage(codeComponent.getCodeMsg("USER_9999_CODE"));
			restExtemalService.insertTbUserOutlinkHist(map, UserCode.USER_9999_CODE, codeComponent.getCodeMsg("USER_9999_CODE"));
		}
		
		return resultVO;
	}
	
	
	
	
	
}
