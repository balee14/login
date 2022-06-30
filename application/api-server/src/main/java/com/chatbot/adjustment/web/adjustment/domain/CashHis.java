package com.chatbot.adjustment.web.adjustment.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "mall_cash_his")
public class CashHis {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private long mall_id;

	@Column(nullable = false, columnDefinition = "이전 캐쉬")
	private int old_cash;

	@Column(nullable = false, columnDefinition = "이후 캐쉬")
	private int new_cash;

	@Column(nullable = false, columnDefinition = "캐쉬 변동액")
	private int cash_changed;

	@Column(nullable = false, columnDefinition = "무료 캐쉬 여부")
	private boolean is_free;

	@Column(nullable = false, columnDefinition = "충전 여부")
	private boolean is_charge;

	public void addHis(long mallID, int oldCash, int newCash, boolean isFree, boolean isCharge) {

		this.mall_id = mallID;
		this.old_cash = oldCash;
		this.new_cash = newCash;
		if(isCharge)
			this.cash_changed = newCash - oldCash;
		else
			this.cash_changed = oldCash - newCash;
		this.is_free = isFree;
		this.is_charge = isCharge;
	}
}
