package com.chatbot.adjustment.batch.mapper;

import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CashRowMapper implements RowMapper {

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		BillingStat billingStat = new BillingStat(
				rs.getString("date"),
				rs.getLong("mall_id"),
				rs.getLong("plus_free"),
				rs.getLong("plus_pay"),
				rs.getLong("minus_free"),
				rs.getLong("minus_pay")
		);

		return billingStat;
	}
}
