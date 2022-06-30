package com.chatbot.adjustment.web.adjustment.repository;

import com.chatbot.adjustment.web.adjustment.domain.CdrHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CdrHisRepository extends JpaRepository<CdrHis, Integer> {

	@Query("SELECT " +
			"   count(*) as cnt " +
			" FROM " +
			"   CdrHis c " +
			" WHERE " +
			"   str_to_date(:from, '%Y%m%d%H') >= c.created_at " +
			"   AND str_to_date(:to, '%Y%m%d%H') < c.created_at")
	Object[] getCount(@Param("from") String from, @Param("to") String to);



	@Query("SELECT c FROM CdrHis c WHERE mall_id = :mall_id AND session = :session")
	CdrHis findByMallAndSession(@Param("mall_id") long mallID, @Param("session") String session);
}
