package com.chatbot.adjustment.web.homepage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;

public interface TbUserOutlinkRepository extends JpaRepository<TbUserOutlink, Long> {
	
	//user_login(ibotId)와 adver_id(광고주ID)와 service_type(서비스타입)를 이용하여 데이터 조회 
	@Query("SELECT u FROM TbUserOutlink u WHERE u.user_login = :user_login AND u.adver_id = :adver_id AND u.service_type = :service_type")
	TbUserOutlink findByUserLoginAndAdverIdAndServiceType(@Param("user_login") String user_login, @Param("adver_id") String adver_id, @Param("service_type") String service_type);
	
	//adverId 중복체크 : 광고주ID가 이미 사용중인지 데이터 조회하여 체크
	@Query("SELECT u FROM TbUserOutlink u WHERE u.adver_id = :adver_id AND u.service_type = :service_type")
	TbUserOutlink findByAdverIdAndServiceType(@Param("adver_id") String adver_id, @Param("service_type") String service_type);
	
	//ibotId 중복체크 : 아이봇ID가 이미 사용중인지 데이터 조회하여 체크
	@Query("SELECT u FROM TbUserOutlink u WHERE u.user_login = :user_login AND u.service_type = :service_type")
	TbUserOutlink findByUserLoginAndServiceType(@Param("user_login") String user_login, @Param("service_type") String service_type);
	
	//ibotId(아이봇아이디)를 이용하여 외부 연동된 정보 조회
	@Query("SELECT u FROM TbUserOutlink u WHERE u.user_login = :user_login AND u.adver_id != ''")
	List<TbUserOutlink> getTbUserOutlinkList(@Param("user_login") String user_login);
	
}