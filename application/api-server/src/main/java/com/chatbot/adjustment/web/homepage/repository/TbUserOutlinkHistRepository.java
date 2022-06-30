package com.chatbot.adjustment.web.homepage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chatbot.adjustment.web.homepage.domain.TbUserOutlink;
import com.chatbot.adjustment.web.homepage.domain.TbUserOutlinkHist;

public interface TbUserOutlinkHistRepository extends JpaRepository<TbUserOutlinkHist, Long> {

}