package com.chatbot.adjustment.web.adjustment.domain;

import com.chatbot.adjustment.web.adjustment.domain.code.PaymentType;
import com.chatbot.adjustment.web.homepage.domain.TbUser;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@Table(name = "mall_billing_stat",
		uniqueConstraints = {
			@UniqueConstraint(
					columnNames = {"date", "mall_id"}
			)
})
public class BillingStat {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false, length = 10)
	private String date;

	@Column(nullable = false)
	private Long mall_id;

	@Transient
	private long payment_total;

	@Column(nullable = false, columnDefinition = "결제 현황-전자결제")
	private long payment_credit;

	@Column(nullable = false, columnDefinition = "결제 현황-무통장입금")
	private long payment_bankbook;

	@Transient
	private long cash_plus_total;

	@Column(nullable = false, columnDefinition = "캐시 충전 금액-무료")
	private long cash_plus_free;

	@Column(nullable = false, columnDefinition = "캐시 충전 금액-유료")
	private long cash_plus_pay;

	@Transient
	private long cash_minus_total;

	@Column(nullable = false, columnDefinition = "캐시 차감 금액-무료")
	private long cash_minus_free;

	@Column(nullable = false, columnDefinition = "캐시 차감 금액-유료")
	private long cash_minus_pay;

	@Transient
	private long cash_remain_total;

	@Column(nullable = false, columnDefinition = "캐시 잔액-무료")
	private long cash_remain_free;

	@Column(nullable = false, columnDefinition = "캐시 잔액-유료")
	private long cash_remain_pay;

	@Column(nullable = false, columnDefinition = "무료 CDR 카운트")
	private int free_cdr_count = 0;

	@Column(nullable = false, columnDefinition = "채팅 서버 CDR 카운트")
	private int chat_cdr_count = 0;

	@Column(nullable = false, columnDefinition = "통계 기록 여부")
	private int is_stat_record;

	@Transient
	private String mallName;

	@Transient
	private List<String> list;

	public BillingStat() {
	}

	public BillingStat(long mallID, String date) {
		this.mall_id = mallID;
		this.date = date;
	}

	public BillingStat(String date, long payment_credit, long payment_bankbook,
	                   long cash_plus_free, long cash_plus_pay,
	                   long cash_minus_free, long cash_minus_pay,
	                   long cash_remain_free, long cash_remain_pay) {
		this.date = date;
		setStat(payment_credit, payment_bankbook, cash_plus_free, cash_plus_pay, cash_minus_free, cash_minus_pay, cash_remain_free, cash_remain_pay);
	}

	public BillingStat(long mall_id, long payment_credit, long payment_bankbook,
	                   long cash_plus_free, long cash_plus_pay,
	                   long cash_minus_free, long cash_minus_pay,
	                   long cash_remain_free, long cash_remain_pay) {
		this.mall_id = mall_id;
		setStat(payment_credit, payment_bankbook, cash_plus_free, cash_plus_pay, cash_minus_free, cash_minus_pay, cash_remain_free, cash_remain_pay);
	}

	private void setStat(long payment_credit, long payment_bankbook,
	                     long cash_plus_free, long cash_plus_pay,
	                     long cash_minus_free, long cash_minus_pay,
	                     long cash_remain_free, long cash_remain_pay) {
		this.payment_total = payment_credit + payment_bankbook;
		this.payment_credit = payment_credit;
		this.payment_bankbook = payment_bankbook;
		this.cash_plus_total = cash_plus_free + cash_plus_pay;
		this.cash_plus_free = cash_plus_free;
		this.cash_plus_pay = cash_plus_pay;
		this.cash_minus_total = cash_minus_free + cash_minus_pay;
		this.cash_minus_free = cash_minus_free;
		this.cash_minus_pay = cash_minus_pay;

		this.cash_remain_free = cash_remain_free;
		this.cash_remain_pay = cash_remain_pay;
		this.cash_remain_total = cash_remain_free + cash_remain_pay;

		list = new ArrayList<>();

		list.add(this.date);
		list.add(String.valueOf(this.payment_total));
		list.add(String.valueOf(this.payment_credit));
		list.add(String.valueOf(this.payment_bankbook));
		list.add(String.valueOf(this.cash_plus_total));
		list.add(String.valueOf(this.cash_plus_free));
		list.add(String.valueOf(this.cash_plus_pay));
		list.add(String.valueOf(this.cash_minus_total));
		list.add(String.valueOf(this.cash_minus_free));
		list.add(String.valueOf(this.cash_minus_pay));
		list.add(String.valueOf(this.cash_remain_total));
		list.add(String.valueOf(this.cash_remain_free));
		list.add(String.valueOf(this.cash_remain_pay));
	}

	public BillingStat(String date, long mall_id, long payment_credit, long payment_bankbook) {
		this.date = date;
		this.mall_id = mall_id;
		this.payment_credit = payment_credit;
		this.payment_bankbook = payment_bankbook;
	}

	public BillingStat(String date, long mall_id, long cash_plus_free, long cash_plus_pay, long cash_minus_free, long cash_minus_pay) {
		this.date = date;
		this.mall_id = mall_id;
		this.cash_plus_free = cash_plus_free;
		this.cash_plus_pay = cash_plus_pay;
		this.cash_minus_free = cash_minus_free;
		this.cash_minus_pay = cash_minus_pay;
	}

	public void mappingMallName(List<TbUser> tbUser) {

		TbUser user = tbUser.stream()
				.filter(it -> this.mall_id.longValue() == it.getSso_mall_id())
				.findFirst().orElse(null);

		this.mallName = "";

		if(user != null) {
			this.mallName = user.getBiz_name();
		}
	}

	public void consumeCash(int newCash, int cash, boolean isFree) {
		if(isFree) {
			this.cash_minus_free = this.cash_minus_free + cash;
			this.cash_remain_free = newCash;
		} else {
			this.cash_minus_pay = this.cash_minus_pay + cash;
			this.cash_remain_pay = newCash;
		}
	}

	public void addCash(int newCash, int cash, boolean isFree) {
		if(isFree) {
			this.cash_plus_free = this.cash_plus_free + cash;
			this.cash_remain_free = newCash;
		} else {
			this.cash_plus_pay = this.cash_plus_pay + cash;
			this.cash_remain_pay = newCash;
		}
	}

	public void increaseFreeCdrCount() {
		this.free_cdr_count++;
	}

	public void addPayment(PaymentType paymentType, int cash) {
		int paymentAmount = cash * 1;
		if(paymentType == PaymentType.CREDIT)
			this.payment_credit = paymentAmount;
		else
			this.payment_bankbook = paymentAmount;
	}
}
