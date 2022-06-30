package com.chatbot.adjustment.web.adjustment.domain;

import com.chatbot.adjustment.kafka.domain.CdrUnit;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "mall_cdr_his",
		uniqueConstraints = {
				@UniqueConstraint(
						columnNames = {"mall_id", "session"}
				)
		})
public class CdrHis {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private long mall_id;

	@Column(nullable = false)
	private String mall_user;

	@Column(nullable = false)
	private String session;

	@Column(nullable = false)
	private Date created_at;

	public void setHis(CdrUnit cdr) {
		this.mall_id = cdr.getMall();
		this.mall_user = cdr.getUser();
		this.session = cdr.getSession();
		this.created_at = new Date(cdr.getDate());
	}
}
