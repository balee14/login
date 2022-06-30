package com.chatbot.adjustment.web.homepage.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.adjustment.web.homepage.domain.TbUser;

public interface TbUserRepository extends JpaRepository<TbUser, Long> {
	
	@Query(value="SELECT u FROM TbUser u WHERE u.user_login = :user_login")
	TbUser findByUserLogin(@Param("user_login") String user_login);
	
	@Query(value="SELECT count(u) FROM TbUser u WHERE u.user_login = :user_login AND u.access_token = :access_token AND u.exp_dt > DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s')")
	int countByUserLoginToken(@Param("user_login") String user_login, @Param("access_token") String access_token);
	
	@Query(value="SELECT u FROM TbUser u WHERE u.user_login = :user_login AND u.access_token = :access_token AND u.exp_dt > DATE_FORMAT(now(), '%Y-%m-%d %H:%i:%s')")
	TbUser findByUserLoginToken(@Param("user_login") String user_login, @Param("access_token") String access_token);
	
	
	@Query("SELECT u FROM TbUser u WHERE u.sso_user_id = :sso_user_id")
	TbUser findBySsoUserId(@Param("sso_user_id") long userID);

	//@Query("SELECT u FROM TbUser u WHERE u.user_login = :user_login AND u.user_pass = :user_pass")
	//TbUser findByUserLoginAndUserPass(@Param("user_login") String user_login, @Param("user_pass") String user_pass);
	
	//@Query("SELECT u FROM TbUser u WHERE u.user_login = :user_login AND u.access_token = :access_token")
	//TbUser selectUserLoginToken(@Param("user_login") String user_login, @Param("access_token") String access_token);
	
	@Query("SELECT u FROM TbUser u WHERE u.sso_mall_id in :mall_ids")
	List<TbUser> findListByMallIds(@Param("mall_ids") List<Long> mallIds);

	@Query("SELECT u FROM TbUser u WHERE u.user_status = 1")
	List<TbUser> findActiveAllList();

	@Query("SELECT u FROM TbUser u WHERE u.biz_name = :biz_name")
	TbUser findByBizName(@Param("biz_name") String bizName);

	@Query("SELECT u FROM TbUser u WHERE u.biz_no = :biz_no")
	TbUser findByBizNo(@Param("biz_no") String bizNo);

	@Query("SELECT u FROM TbUser u WHERE u.boss_phone = :boss_phone")
	TbUser findByBossPhone(@Param("boss_phone") String bossPhone);
	
	@Query("SELECT u FROM TbUser u WHERE u.biz_name = :biz_name AND u.biz_no = :biz_no AND u.user_email = :user_email")
	List<TbUser> findByBizNameAndBizNoAndUserEmail(@Param("biz_name") String bizName, @Param("biz_no") String bizNo, @Param("user_email") String userEmail);
	
	@Query("SELECT u FROM TbUser u WHERE u.user_login = :user_login AND u.user_email = :user_email")
	List<TbUser> findByUserLoginAndUserEmail(@Param("user_login") String userLogin, @Param("user_email") String userEmail);
	
	@Query("SELECT u FROM TbUser u WHERE u.biz_name = :biz_name AND u.biz_no = :biz_no AND u.user_email = :user_email AND u.auth_number = :auth_number")
	List<TbUser> findByBizNameAndBizNoAndUserEmailAndAuthNumber(@Param("biz_name") String bizName, @Param("biz_no") String bizNo, @Param("user_email") String userEmail, @Param("auth_number") String authNumber);
	
	@Query("SELECT u FROM TbUser u WHERE u.user_login = :user_login AND u.user_email = :user_email AND u.auth_number = :auth_number")
	List<TbUser> findByUserLoginAndUserEmailAndAuthNumber(@Param("user_login") String userLogin, @Param("user_email") String userEmail, @Param("auth_number") String authNumber);
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)	
	@Modifying
	@Query("UPDATE TbUser SET access_token = :access_token, exp_dt = :exp_dt WHERE user_login = :user_login")
	int updateTbUserToken(@Param("access_token") String access_token, @Param("exp_dt") Date exp_dt, @Param("user_login") String user_login);
	
	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET user_pass = :user_pass WHERE user_login = :user_login")
	int updateTbUserPassword(@Param("user_login") String user_login, @Param("user_pass") String user_pass);
	
	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET user_pass = :user_pass, auth_number = null, login_failed_cnt = 0 WHERE user_login = :user_login AND auth_number = :auth_number")
	int updateTbUserPasswordByAuthNumber(@Param("user_login") String user_login, @Param("user_pass") String user_pass, @Param("auth_number") String auth_number);
	
	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET user_pass = :user_pass, biz_name = :biz_name, biz_no = :biz_no, biz_address = :biz_address, user_name = :user_name, user_email = :user_email, user_phone = :user_phone, agreement003 = :agreement003 WHERE user_login = :user_login")
	int updateTbUserInfo(@Param("user_login") String user_login, @Param("user_pass") String user_pass
						, @Param("biz_name") String biz_name, @Param("biz_no") String biz_no
						, @Param("biz_address") String biz_address, @Param("user_name") String user_name
						, @Param("user_email") String user_email, @Param("user_phone") String user_phone
						, @Param("agreement003") String agreement003);
	
	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET auth_number = :auth_number WHERE biz_name = :biz_name AND biz_no = :biz_no AND user_email = :user_email")
	int updateTbUserAuthNumber(@Param("auth_number") String authNumber, @Param("biz_name") String bizName, @Param("biz_no") String bizNo, @Param("user_email") String userEmail);
	
	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET auth_number = :auth_number WHERE user_login = :user_login AND user_email = :user_email")
	int updateTbUserAuthNumberById(@Param("auth_number") String authNumber, @Param("user_login") String userLogin, @Param("user_email") String userEmail);

	@Transactional
	@Modifying
	@Query("UPDATE TbUser SET agreement004 = :agreement004 WHERE user_login = :user_login")
	int modifyUserInfoWithAgreement(@Param("user_login") String user_login, @Param("agreement004") String agreement004);
}
