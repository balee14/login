package com.chatbot.adjustment.batch.reader;

import com.chatbot.adjustment.batch.mapper.CashRowMapper;
import com.chatbot.adjustment.batch.mapper.PaymentRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.database.JdbcCursorItemReader;

import javax.sql.DataSource;

public class CashItemReader<T> extends JdbcCursorItemReader<T> implements StepExecutionListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private DataSource adjustmentDataSource;

	public CashItemReader(DataSource dataSource) {
		this.adjustmentDataSource = dataSource;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {

		logger.debug("[CashItemReader][beforeStep]");

		JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
		String statDate = parameters.getString("statDate");

		logger.debug("[CashItemReader][beforeStep] statDate : " + statDate);
		logger.debug("[CashItemReader][beforeStep] adjustmentDataSource : " + adjustmentDataSource);

		this.setDataSource(adjustmentDataSource);
		this.setName("cash-reader");
		this.setSql("SELECT " +
					"   date, mall_id, max(plus_pay) as plus_pay, max(plus_free) as plus_free, max(minus_pay) as minus_pay, max(minus_free) as minus_free " +
					" FROM " +
					" ( " +
					"   SELECT " +
					"       date_format(created_at, '%Y%m%d%H') as date, mall_id, " +
					"       case when is_charge = 1 and is_free = 0 then sum(cash_changed) else 0 end as plus_pay, " +
					"       case when is_charge = 1 and is_free = 1 then sum(cash_changed) else 0 end as plus_free, " +
					"       case when is_charge = 0 and is_free = 0 then sum(cash_changed) else 0 end as minus_pay, " +
					"       case when is_charge = 0 and is_free = 1 then sum(cash_changed) else 0 end as minus_free " +
					"   FROM " +
					"       mall_cash_his " +
					"   WHERE " +
					"       date_add(str_to_date('" + statDate + "', '%Y%m%d%H'), interval +1 hour) <= created_at " +
					"   GROUP BY " +
					"       date_format(created_at, '%Y%m%d%H'), mall_id, cash_changed, is_free, is_charge " +
					" ) a " +
					" GROUP BY " +
					"   date, mall_id");

		this.setRowMapper(new CashRowMapper());

	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}
}
