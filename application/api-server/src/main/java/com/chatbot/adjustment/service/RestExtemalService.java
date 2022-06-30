package com.chatbot.adjustment.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;
import com.chatbot.adjustment.web.homepage.domain.TbUserOutlinkHist;
import com.chatbot.adjustment.web.homepage.repository.TbUserOutlinkHistRepository;
import com.chatbot.adjustment.web.homepage.repository.TbUserOutlinkRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class RestExtemalService {

	private static final Logger logger = LoggerFactory.getLogger(RestExtemalService.class);
	
	//JWT 토큰 만들 때 필요한 key 값 (로그인 소스내에도 존재해서 동일하게 맞춤)
	private String TOKEN_SECRET_KEY = "TNDtMCEn_TNSTvba7T16E1pK9DxcMa1pMpjJ9IYkmeTHGtcqnkIQ9WmiqeYQeIn6kyfs-UIGiw1hRqdv3BHa5mUqVn0536wDcJFB_niO6evPjt4mi5veMmx4wvGKYvQu3v2_zn0rNz43srWJYWZ7yJrSb_Dsgaw7SIF9fukvV7RQlI-kAdBnyo0y9KMNT4eZJJ39c-cJK9pYzzN7lYSBzLfqdolCiC6qpafIVowJlcQlrhLCtNdIQuSZ6cYBSjDhKTT-R07TG1ZmQRCeyP-7ummKoV1zMeKJGRziAng50U1qjEVSEZFn9d_ThlZj02G-on0VYAxZzs0XzXV8wN7I1g";
	
	@Autowired
	private TbUserOutlinkHistRepository tbUserOutlinkHistRepository;
	
	@Autowired
	private TbUserOutlinkRepository tbUserOutlinkRepository;
	
	/**
	 * @param map
	 * @param resultCode
	 * @param resultMessage
	 */
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
		
		tbUserOutlinkHist.setUser_login(ibotId); //옵션
		tbUserOutlinkHist.setAdver_id(adverId);  //옵션
		tbUserOutlinkHist.setService_type(serviceType); //필수
		tbUserOutlinkHist.setSuccess_yn(successYn);		//필수
		tbUserOutlinkHist.setCreated_dt(new Date());	//필수
		tbUserOutlinkHist.setUpdated_dt(new Date());	//필수
		tbUserOutlinkHist.setResult_code(resultCode);	//필수
		tbUserOutlinkHist.setResult_message(resultMessage);	//필수
		tbUserOutlinkHist.setRequest_url(requestUrl);		//필수
		tbUserOutlinkHist.setRequest_ip(requestIp);			//필수
		tbUserOutlinkHist.setToken(token);  	  		//옵션
		tbUserOutlinkHist.setExpired_dt(expriedDate); 	//옵션
		tbUserOutlinkHist.setAgreement001(agreement001);//옵션
		tbUserOutlinkHist = tbUserOutlinkHistRepository.save(tbUserOutlinkHist);
	}
	
	/**
	 * @param ibotId
	 * @param adverId
	 * @param serviceType
	 * @return
	 */
	public TbUserOutlink getTbUserOutlink(String ibotId, String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByUserLoginAndAdverIdAndServiceType(ibotId, adverId, serviceType);
	}
	
	/**
	 * @param adverId
	 * @param serviceType
	 * @return
	 */
	public TbUserOutlink getTbUserOutlink(String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByAdverIdAndServiceType(adverId, serviceType);
	}
	
	/**
	 * @param userId
	 * @return
	 */
	public List<TbUserOutlink> getTbUserOutlinkList(String userId) {
		return tbUserOutlinkRepository.getTbUserOutlinkList(userId);
	}
	
	/**
	 * 수정 : 연동일 경우 adverId가 있음. 해제일 경우 adverId 없음.
	 * @param tbUserOutlink
	 */
	public void updateTbUserOutlink(TbUserOutlink tbUserOutlink) {
		tbUserOutlinkRepository.save(tbUserOutlink);
	}
	
	/**
	 * @param ibotId
	 * @param adverId
	 * @param serviceType
	 * @return
	 */
	public TbUserOutlink mergeTbUserOutlink(String ibotId, String adverId, String serviceType) {
		
		//tb_user.user_login == tb_user_outlink.user_login(ibot 아이디)
		//1:1 매핑으로 아이봇아이디,서비스타입 기준으로 정보가 존재하면 update, 없으면 insert
		TbUserOutlink tbUserOutlinkData = tbUserOutlinkRepository.findByUserLoginAndServiceType(ibotId, serviceType);
		TbUserOutlink tbUserOutlink = new TbUserOutlink();
		
		if(tbUserOutlinkData == null) {
			logger.error("[UserService.java][mergeTbUserOutlink] tb_user_outlink에 adverId 미존재.");
			tbUserOutlink.setUser_login(ibotId);
			tbUserOutlink.setAdver_id(adverId);
			tbUserOutlink.setService_type(serviceType);
			tbUserOutlink.setCreated_dt(new Date());
			tbUserOutlink.setUpdated_dt(new Date());
			tbUserOutlink.setAgreement001("Y");
			tbUserOutlink = tbUserOutlinkRepository.save(tbUserOutlink);
		} else {
			logger.error("[UserService.java][mergeTbUserOutlink] tb_user_outlink에 adverId 존재.");
			tbUserOutlinkData.setAdver_id(adverId);
			tbUserOutlinkData.setService_type(serviceType);
			tbUserOutlinkData.setUpdated_dt(new Date());
			tbUserOutlinkData.setAgreement001("Y");
			tbUserOutlink = tbUserOutlinkRepository.save(tbUserOutlinkData);
		}
		
		return tbUserOutlink;
	}
	
	/**
	 * @param adverId
	 * @param serviceType
	 * @return
	 */
	public TbUserOutlink checkAdverIdDup(String adverId, String serviceType) {
		return tbUserOutlinkRepository.findByAdverIdAndServiceType(adverId, serviceType);
	}
	
	/**
	 * @param ibotId
	 * @param serviceType
	 * @return
	 */
	public TbUserOutlink checkIbotIdDup(String ibotId, String serviceType) {
		return tbUserOutlinkRepository.findByUserLoginAndServiceType(ibotId, serviceType);
	}
	
	/**
	 * 
	 * @param user_login
	 * @param adver_id
	 * @return
	 */
	public String createOutLinkToken(String user_login, String adver_id) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 30); //토큰 유지 기간:30일(한달)
		Date ontMonthAfterDate = c.getTime();
		long ontMonthAfterDateTime = ontMonthAfterDate.getTime();
		long ontMonthAfterTimeStamp = (long) (ontMonthAfterDateTime * 0.001);  //unix timestamp (/1000 을 하는 것보다 *가 더 효율이 낫다)
		
		//case1.AES256 암호화
		/*
		Aes256 aes = new Aes256();
		aes.setKey(LOGIN_TOKEN_KEY);
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("user_login", tbUserOutlink.getUser_login());
		dataMap.put("adver_id", tbUserOutlink.getAdver_id());
		dataMap.put("expired_dt", ontMonthAfterTimeStamp);
		
		String jsonString = JSONUtil.mapToJson(dataMap);
		String enc_str = aes.encrypt(jsonString); //암호화
		*/
		//case2.JWT 생성
		String token = "";
		//1.header
		Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        headers.put("alg", "HS256");
        
		//2.Payload
	    Map<String, Object> payloads = new HashMap<>();
		payloads.put("user_login", user_login);
		payloads.put("adver_id", adver_id);
		payloads.put("expired_dt", ontMonthAfterTimeStamp);
		
	    // 토큰 Builder
		token = Jwts.builder()
                .setHeader(headers) // Headers 설정
                .setClaims(payloads) // Claims 설정
                //.setSubject("user") // 토큰 용도 
                .setExpiration(ontMonthAfterDate) // 토큰 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, TOKEN_SECRET_KEY.getBytes()) // HS256과 Key로 Sign
                .compact(); // 토큰 생성
		
        return token;
	}
	
	
}
