package com.chatbot.login.login.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long user_id;

	@Column(length = 25, nullable = false, unique = true)
	private String username;

	@Column(length = 64, nullable = false)
	private String password;

	@Column(length = 1, nullable = false)
	private boolean enabled;

	//1:주관리자, 2:부관리자, 3:상담사, 4:내부관리자
	@Column(length = 1, nullable = false)
	private String user_type;

	@Column(nullable = false)
	private long mall_id;

	@Column(nullable = false)
	private int password_failed_count;
}
