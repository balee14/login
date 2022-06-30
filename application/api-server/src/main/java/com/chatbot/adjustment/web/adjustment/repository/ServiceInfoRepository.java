package com.chatbot.adjustment.web.adjustment.repository;

import com.chatbot.adjustment.web.adjustment.domain.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceInfoRepository extends JpaRepository<ServiceInfo, Long> {

	@Query("SELECT s FROM ServiceInfo s WHERE mall_id = :mall_id AND sysdate() < s.free_service_end_dt")
	ServiceInfo findByFreeService(@Param("mall_id") long mallID);

	@Query("SELECT s FROM ServiceInfo s WHERE s.first_payment_at is NOT NULL")
	List<ServiceInfo> findListByPaymentService();

}
