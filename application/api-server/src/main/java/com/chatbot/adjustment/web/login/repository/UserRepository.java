package com.chatbot.adjustment.web.login.repository;

import com.chatbot.adjustment.web.login.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.username = :username")
	User findByUsername(@Param("username") String username);
	
	@Transactional
	@Modifying
	@Query("UPDATE User SET password = :password, password_failed_count = :password_failed_count WHERE username = :username")
	int updatePassword(@Param("username") String username, @Param("password") String password, @Param("password_failed_count") int password_failed_count);
}
