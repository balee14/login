package com.chatbot.adjustment.web.homepage.domain;

import lombok.Data;

import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Data
@Table(name = "tb_user_outlink",
	uniqueConstraints = {
		@UniqueConstraint(
				columnNames = {"user_login", "adver_id"}
		)
})
public class TbUserOutlink {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long seq;

	@Column(length = 60, nullable = false)
	private String user_login;

	@Column(length = 100, nullable = false)
	private String adver_id;

	@Column(length = 10, nullable = false)
	private String service_type;

	@Column(nullable = false)
	private Date created_dt;
	
	@Column(nullable = false)
	private Date updated_dt;
	
	@Column(length = 1000, nullable = true)
	private String token;

	@Column(nullable = true)
	private Date expired_dt;
	
	@Column(nullable = false)
	private String agreement001;
	
	@Override
	public String toString() {
		 return ToStringBuilder.reflectionToString(this).toString();
    }
}
