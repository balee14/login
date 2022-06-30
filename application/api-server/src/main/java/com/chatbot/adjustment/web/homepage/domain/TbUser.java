package com.chatbot.adjustment.web.homepage.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Data;

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

	@Column(nullable = false)
	private int user_status;

	private String agreement001 = null;
	private String agreement002 = null;
	private String agreement003 = null;
	private String agreement004 = null;
	private long sso_user_id;
	private long sso_mall_id;
	private Integer sso_user_role = null;
	private Integer login_failed_cnt = null;
	private String biz_name;
	private String biz_no;
	private String biz_domain;
	private String biz_address;
	private String boss_name;
	private String boss_phone;
	private String sso_username_markered;
	private String user_name = null;
	private String user_phone = null;
	private String access_token = null;
	private String auth_number = null;
	private long recommend_id;
	private Date exp_dt;
	
	@Override
	public String toString() {
		 return ToStringBuilder.reflectionToString(this).toString();
    }
}
