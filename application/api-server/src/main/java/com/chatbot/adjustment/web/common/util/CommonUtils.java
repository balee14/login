package com.chatbot.adjustment.web.common.util;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CommonUtils {
	
	static Logger logger = LoggerFactory.getLogger("TokenInfoGet.class");
	public static final String profile = System.getProperty("spring.profiles.active");
	
	public static String getIp() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        
        logger.debug(">>>> X-FORWARDED-FOR : " + ip);
        if("live".equals(profile)) logger.error(">>>> X-FORWARDED-FOR : " + ip); //상용확인용
        
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
            logger.debug(">>>> Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
            logger.debug(">>>> WL-Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            logger.debug(">>>> HTTP_CLIENT_IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            logger.debug(">>>> HTTP_X_FORWARDED_FOR : " + ip);
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
       
        logger.debug(">>>> Result : IP Address : "+ip);
        if("live".equals(profile)) logger.error(">>>> Result : IP Address : "+ip); //상용확인용
        
        return ip;
        
    }
	
	/**
     * 숫자 랜덤 생성(len: 생성할 난수의 길이, dupCd: 중복허용 여부(1: 중복허용, 2: 중복제거))
     * @param len
     * @param dupCd
     * @return
     */
    public static String numberGen(int len, int dupCd) {
        
        Random rand = new Random();
        String numStr = "";
        
        for (int i=0, l=len; i<l; i++) {
            
            // 0~9 까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));
            
            // 중복 허용시 그대로 추가
            if (dupCd == 1) {
                numStr += ran;
            }
            // 중복을 허용하지 않을시 중복된 값이 있는지 체크
            else if (dupCd == 2) {
                
            	// 중복된 값이 없으면 numStr에 append
                if (! numStr.contains(ran)) {
                    numStr += ran;
                }
                // 생성된 난수가 중복되면 루틴을 다시 실행한다
                else {
                    i -= 1;
                }
            }
        }
        
        return numStr;
    }
	
	
	
}
