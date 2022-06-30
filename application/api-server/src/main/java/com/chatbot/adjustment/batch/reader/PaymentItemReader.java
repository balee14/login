package com.chatbot.adjustment.batch.reader;

import com.chatbot.adjustment.batch.mapper.PaymentRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component("paymentItemReader")
@Scope("step")
public class PaymentItemReader<T> extends JdbcCursorItemReader<T> implements StepExecutionListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private DataSource homepageDataSource;

	public PaymentItemReader(DataSource dataSource) {
		this.homepageDataSource = dataSource;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {

		logger.debug("[PaymentItemReader][beforeStep]");

		JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
		String statDate = parameters.getString("statDate");

		logger.debug("[PaymentItemReader][beforeStep] statDate : " + statDate);
		logger.debug("[PaymentItemReader][beforeStep] homepageDataSource : " + homepageDataSource);

		this.setDataSource(homepageDataSource);
		this.setName("payment-reader");
		this.setSql("SELECT " +
					"   date, user_id, max(bankbook) as bankbook, max(credit) as credit " +
					" FROM " +
					" ( " +
					"       SELECT " +
					"           date_format(paid_date, '%Y%m%d%H') as date, user_id, " +
					"           case when pay_type = '00001' then sum(amount) else 0 end as bankbook, " +
					"           case when pay_type = '00003' then sum(amount) else 0 end as credit" +
					"       FROM " +
					"           pb_cash_order " +
					"       WHERE " +
					"           service_type = '00005' " +
					"           and status = '00005' " +
					"           and pay_type != '' " +
					"           and date_add(str_to_date('" + statDate + "', '%Y%m%d%H'), interval +1 hour) <= paid_date " +
					"       GROUP BY " +
					"           date_format(paid_date, '%Y%m%d%H'), user_id, pay_type " +
					" ) a " +
					"GROUP BY " +
					"   date, user_id");

		this.setRowMapper(new PaymentRowMapper());
	}

	/*@Override
	public Payment read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

	}*/

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}
}
