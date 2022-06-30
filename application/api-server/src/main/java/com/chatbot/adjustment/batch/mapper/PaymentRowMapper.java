package com.chatbot.adjustment.batch.mapper;

import com.chatbot.adjustment.batch.domain.Payment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentRowMapper implements RowMapper {

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Payment payment = new Payment(
				rs.getString("date"),
				rs.getLong("user_id"),
				rs.getLong("bankbook"),
				rs.getLong("credit")
		);

		return payment;
	}
}
