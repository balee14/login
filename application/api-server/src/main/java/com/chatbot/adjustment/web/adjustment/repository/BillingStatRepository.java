package com.chatbot.adjustment.web.adjustment.repository;

import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillingStatRepository extends JpaRepository<BillingStat, Long> {

	static final String SELECT_QUERY_BY_DATE = "SELECT " +
												"   new BillingStat(" +
												"   substring(b.date, 1, 8), " +
												"   sum(b.payment_credit), " +
												"   sum(b.payment_bankbook), " +
												"   sum(b.cash_plus_free), " +
												"   sum(b.cash_plus_pay), " +
												"   sum(b.cash_minus_free), " +
												"   sum(b.cash_minus_pay), " +
												"   sum(b.cash_remain_free), " +
												"   sum(b.cash_remain_pay) " +
												"   ) " +
												" FROM " +
												"   BillingStat b ";

	static final String SELECT_QUERY_BY_MALL = "SELECT " +
			"   new BillingStat(" +
			"   mall_id, " +
			"   sum(b.payment_credit), " +
			"   sum(b.payment_bankbook), " +
			"   sum(b.cash_plus_free), " +
			"   sum(b.cash_plus_pay), " +
			"   sum(b.cash_minus_free), " +
			"   sum(b.cash_minus_pay), " +
			"   sum(b.cash_remain_free), " +
			"   sum(b.cash_remain_pay) " +
			"   ) " +
			" FROM " +
			"   BillingStat b ";

	@Query(SELECT_QUERY_BY_DATE +
			" WHERE " +
			"   substring(b.date, 1, 8) >= :from_date AND substring(b.date, 1, 8) <= :to_date " +
			" GROUP BY " +
			"   substring(b.date, 1, 8) " +
			" ORDER BY " +
			"   substring(date, 1, 8) DESC")
	List<BillingStat> search(@Param("from_date") String fromDate, @Param("to_date") String toDate);

	@Query(SELECT_QUERY_BY_DATE +
			" WHERE " +
			"   substring(b.date, 1, 6) = :ym " +
			" GROUP BY " +
			"   substring(b.date, 1, 8)" +
			" ORDER BY " +
			"   substring(date, 1, 8) DESC")
	List<BillingStat> searchMonth(@Param("ym") String ym);

	@Query(SELECT_QUERY_BY_DATE +
			" WHERE" +
			"   mall_id = :mall_id " +
			"   AND substring(b.date, 1, 8) >= :from_date AND substring(b.date, 1, 8) <= :to_date " +
			" GROUP BY " +
			"   substring(b.date, 1, 8) " +
			" ORDER BY " +
			"   substring(date, 1, 8) DESC")
	List<BillingStat> search(@Param("from_date") String fromDate, @Param("to_date") String toDate, @Param("mall_id") long mallID);

	@Query(SELECT_QUERY_BY_DATE +
			" WHERE " +
			"   mall_id = :mall_id " +
			"   AND substring(b.date, 1, 6) = :ym " +
			" GROUP BY " +
			"   substring(b.date, 1, 8)" +
			" ORDER BY " +
			"   substring(date, 1, 8) DESC")
	List<BillingStat> searchMonth(@Param("ym") String ym, @Param("mall_id") long mallID);


	@Query(SELECT_QUERY_BY_MALL +
			" WHERE " +
			"   substring(b.date, 1, 8) >= :from_date AND substring(b.date, 1, 8) <= :to_date " +
			" GROUP BY " +
			"   b.mall_id " +
			" ORDER BY " +
			"   b.mall_id")
	List<BillingStat> searchForMall(@Param("from_date") String fromDate, @Param("to_date") String toDate);

	@Query(SELECT_QUERY_BY_MALL +
			" WHERE " +
			"   substring(b.date, 1, 6) = :ym " +
			" GROUP BY " +
			"   b.mall_id " +
			" ORDER BY " +
			"   b.mall_id")
	List<BillingStat> searchMonthForMall(@Param("ym") String ym);

	@Query("SELECT max(b.date) as date FROM BillingStat b WHERE b.is_stat_record = 1 ")
	Object[] getMaxStatDate();

	@Query("SELECT b FROM BillingStat b WHERE b.mall_id = :mall_id AND b.date = :date")
	BillingStat findByMallAndDate(@Param("mall_id") long mallID, @Param("date") String date);

	@Query("SELECT b FROM BillingStat b " +
			" WHERE " +
			"   substring(b.date, 1, 8) >= :from_date AND substring(b.date, 1, 8) <= :to_date " +
			"   AND (b.cash_minus_free + b.cash_minus_pay) != b.chat_cdr_count " +
			" ORDER BY " +
			"   b.id DESC")
	List<BillingStat> findMisMatchCdrCount(@Param("from_date") String fromDate, @Param("to_date") String toDate);

	@Query("SELECT b FROM BillingStat b " +
			" WHERE " +
			"   substring(b.date, 1, 6) = :ym " +
			"   AND (b.cash_minus_free + b.cash_minus_pay + b.free_cdr_count) != b.chat_cdr_count " +
			" ORDER BY " +
			"   b.id DESC")
	List<BillingStat> findMisMatchCdrCount(@Param("ym") String ym);
}
