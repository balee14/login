package com.chatbot.login.homepage.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "tb_user")
public class TbUser {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(length = 60, nullable = false)
	private String user_login;

	@Column(length = 255, nullable = false)
	private String user_pass;

	@Column(length = 100, nullable = false)
	private String user_email;

	private String biz_name;

	private String biz_no;

	private String biz_domain;

	private String boss_name;

	private String boss_phone;

	private Long sso_user_id;

	private String sso_username_markered;

}
