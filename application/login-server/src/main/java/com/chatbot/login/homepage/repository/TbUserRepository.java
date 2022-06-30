package com.chatbot.login.homepage.repository;

import com.chatbot.login.homepage.domain.TbUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TbUserRepository extends JpaRepository<TbUser, Long> {

	@Query("select u from TbUser u where u.sso_user_id = :sso_user_id")
	TbUser findBySsoUserId(@Param("sso_user_id") Long userID);
	
	@Query("select u from TbUser u where u.user_login = :user_login")
	TbUser findByUsername(@Param("user_login") String user_login);
}
