package com.chatbot.adjustment.web.login.domain;

import com.chatbot.adjustment.web.login.domain.code.UserType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@ApiModelProperty(notes = "", name = "user_id", required = false, value = "사용자 고유키")
	private long user_id;

	@Column(length = 25, nullable = false, unique = true)
	@ApiModelProperty(notes = "", name = "username", required = true, value = "사용자 이름(아이디)")
	private String username; //영문/숫자 조합 8자 이상 15자 이내

	@Column(length = 64, nullable = false)
	@ApiModelProperty(notes = "", name = "password", required = true, value = "사용자 패스워드")
	private String password; //영문/숫자/특수문자 조합 8자 이상 15자 이내

	@Column(length = 1, nullable = false)
	private boolean enabled = true;

	//1:주관리자, 2:부관리자, 3:상담사, 4:내부관리자
	@ApiModelProperty(notes = "", name = "user_type", required = true, value = "사용자 타입(1:주관리자, 2:부관리자, 3:상담사, 4:내부관리자, 5:홈페이지관리자)")
	@Column(length = 1, nullable = false)
	private String user_type;

	@ApiModelProperty(notes = "", name = "mall_id", required = false, value = "몰 ID(부관리자나 상담사의 경우 주관리자의 몰 ID)")
	@Column(nullable = false)
	private long mall_id;

	@Column(nullable = false)
	private int password_failed_count;

	public User() {
	}

	public User(String userName, String password) {
		this.username = userName;
		this.password = password;
	}

	public String getUserTypeDesc() {

		for(UserType userType : UserType.values()) {
			if(userType.getCode().equals(user_type)) {
				return userType.getDesc();
			}
		}
		return "";
	}
}
