package com.chatbot.adjustment.web.adjustment.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "mall_service_info")
public class ServiceInfo {

	public static final int CONSULTING_CASH_AMOUNT = 100; //상담 캐쉬 금액
	public static final int FREE_CASH_AMOUNT = 300 * CONSULTING_CASH_AMOUNT; //무료건수 * 상담 캐쉬 금액

	@Id
	@ApiModelProperty(notes = "", name = "mall_id", required = false, value = "몰 ID")
	private long mall_id;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	@Column(nullable = true, columnDefinition = "무료 상담 기간")
	@ApiModelProperty(notes = "", name = "free_service_end_dt", required = false, value = "무료 상담 기간")
	private Date free_service_end_dt;

	@Column(nullable = true, columnDefinition = "무료 캐쉬 사용기간")
	@ApiModelProperty(notes = "", name = "free_cash_avaiable_dt", required = false, value = "무료 캐쉬 사용기간")
	private Date free_cash_avaiable_dt;

	@Column(nullable = false, columnDefinition = "무료 캐쉬")
	@ApiModelProperty(notes = "", name = "free_cash", required = false, value = "무료 캐쉬")
	private int free_cash;

	@Column(nullable = false, columnDefinition = "유료 캐쉬")
	@ApiModelProperty(notes = "", name = "cash", required = false, value = "유료 캐쉬")
	private int cash;

	@Column(nullable = true, columnDefinition = "최초 유료 결제일")
	@ApiModelProperty(notes = "", name = "first_payment_at", required = false, value = "최초 유료 결제일")
	private Date first_payment_at;

	public void setCashByIsFree(boolean isFree, int newCash) {

		if (isFree) {
			setFree_cash(newCash);
		} else {
			setCash(newCash);
		}
	}
}
