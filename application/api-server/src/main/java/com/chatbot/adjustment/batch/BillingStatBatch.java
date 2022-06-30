package com.chatbot.adjustment.batch;

import com.chatbot.adjustment.batch.domain.Payment;
import com.chatbot.adjustment.batch.reader.CashItemReader;
import com.chatbot.adjustment.batch.reader.PaymentItemReader;
import com.chatbot.adjustment.service.BillingStatService;
import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Date;

//@Configuration
//@EnableBatchProcessing
//@EnableScheduling
public class BillingStatBatch {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("adjustmentDataSource")
	private DataSource dataSource;

	@Autowired
	@Qualifier("homepageDataSource")
	private DataSource homepageDataSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

	//@Autowired
	//private JobRepository jobRepository;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	//@Autowired
	//private SimpleJobLauncher jobLauncher;

	@Autowired
	BillingStatService billingStatService;

	/*@Bean
	public DataSource dataSource(@Qualifier("adjustmentDataSource") DataSource dataSource) {
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) throws Exception {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public DataSource homepageDataSource(@Qualifier("homepageDataSource") DataSource dataSource) {
		return dataSource;
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(transactionManager);
		factory.afterPropertiesSet();

		return (JobRepository)factory.getObject();
	}*/

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean = new MapJobRepositoryFactoryBean(transactionManager);
		mapJobRepositoryFactoryBean.setTransactionManager(transactionManager);
		return mapJobRepositoryFactoryBean.getObject();
	}

	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
		SimpleJobLauncher launcher = new SimpleJobLauncher();
		launcher.setJobRepository(jobRepository);
		launcher.afterPropertiesSet();

		return launcher;
	}

	/**
	 * 1. 결제 현황
	 * 결제 정보 테이블(pb_paygate_order)에서 결제 정보 조회
	 * 조회 조건은 mall_billing_stat 에 마지막에 기록된 날짜 가져와서 +1시간 더한 시간이후의 결제 정보를 모두 가져온다.
	 *
	 * 2. 캐시 충전/차감/잔액 -> 배치에서 하지 않고 실제 충전 차감시 바로 업데이트 처리
	 * 캐시 히스토리 테이블(mall_cash_his)에서 조회
	 * 조회 조건은 동일
	 * 캐시 잔액은 충전금액-차감금액
	 *
	 * @throws Exception
	 */
	//@Scheduled(cron = "0 * * * * *") //1분마다
	//@Scheduled(cron = "*/30 * * * * *") //30초마다
	@Scheduled(cron = "0 0 * * * *") //매시간마다
	public void perform() throws Exception {
		logger.debug("Job Started at : " + new Date());

		String statDate = billingStatService.getMaxStatDate();
		logger.debug("[statDate] : " + statDate);

		JobParameters param = new JobParametersBuilder()
				.addString("JobID", String.valueOf(System.currentTimeMillis()))
				.addString("statDate", statDate)
				.toJobParameters();
		JobExecution execution = jobLauncher(jobRepository()).run(job(), param);

		logger.debug("Job Finished with status : " + execution.getStatus());
	}

	@Bean
	public Job job() {
		FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow1");

		Flow flow = flowBuilder
				.start(step1())
				//.next(decision())
				//.on(decision().COMPLETED)
				//.to(step2())
				//.from(decision())
				//.on(decision().COMPLETED)
				//.to(step3())
				.end();

		return jobBuilderFactory.get("readData")
				.incrementer(new RunIdIncrementer())
				.start(flow)
				.end()
				.build();
	}

	@Bean
	public FlowDecision decision() {
		return new FlowDecision();
	}

	@Bean
	public Step step1() {

		return stepBuilderFactory.get("step1")
				.<Payment, Payment>chunk(100)
				.reader(new PaymentItemReader<>(homepageDataSource))
				//.processor()
				.writer(writer1())
				.build();
	}

	public Tasklet tasklet() {

		return null;
	}

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2")
				.<BillingStat, BillingStat>chunk(100)
				.reader(new CashItemReader<>(dataSource))
				//.processor()
				.writer(writer2())
				.build();
	}

	/*@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3")
				.<BillingStat, BillingStat>chunk(100)
				.reader(reader2())
				//.processor()
				.writer(writer2())
				.build();
	}*/

	/*@Bean
	public JdbcCursorItemReader<Payment> reader1() {

		JdbcCursorItemReader<Payment> reader = new JdbcCursorItemReader<>();
		String statDate = "2018041005";
		//String statDate = reader.getStatDate();
		logger.debug("[reader1]sql : " + reader.getSql());

		reader.setDataSource(homepageDataSource);
		reader.setName("reader1");
		reader.setSql("SELECT " +
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
		reader.setRowMapper((rs, i) -> new Payment(rs.getString("date"),
													rs.getLong("user_id"),
													rs.getLong("bankbook"),
													rs.getLong("credit")));

		return reader;
	}*/

	@Bean
	public JdbcBatchItemWriter<Payment> writer1() {
		JdbcBatchItemWriter<Payment> writer = new JdbcBatchItemWriter<Payment>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Payment>());
		writer.setSql("INSERT INTO `mall_billing_stat` (`date`, `mall_id`, `payment_credit`, `payment_bankbook`, `is_stat_record`) " +
						"   VALUES" +
						" (:date, :mall_id, :payment_credit, :payment_bankbook, 1)" +
						" ON DUPLICATE KEY UPDATE " +
						"   payment_credit = :payment_credit, " +
						"   payment_bankbook = :payment_bankbook, " +
						"   is_stat_record = 1 " +
						" ");
		writer.setDataSource(dataSource);

		return writer;
	}



	/*@Bean
	public CashItemReader<BillingStat> reader2() {

		CashItemReader<BillingStat> reader = new CashItemReader<>();
		String statDate = "2018041005";
		//String statDate = reader.getStatDate();
		logger.debug("[reader2]statdate : " + statDate);

		reader.setDataSource(dataSource);
		reader.setName("reader2");
		reader.setSql("SELECT " +
						"   date, mall_id, max(plus_pay) as plus_pay, max(plus_free) as plus_free, max(minus_play) as minus_play, max(minus_free) as minus_free " +
						" FROM " +
						" ( " +
						"   SELECT " +
						"       date_format(created_at, '%Y%m%d%H') as date, mall_id, " +
						"       case when is_charge = 1 and is_free = 0 then sum(cash_changed) else 0 end as plus_pay, " +
						"       case when is_charge = 1 and is_free = 1 then sum(cash_changed) else 0 end as plus_free, " +
						"       case when is_charge = 0 and is_free = 0 then sum(cash_changed) else 0 end as minus_play, " +
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
		reader.setRowMapper((rs, i) -> new BillingStat(rs.getString("date"),
				rs.getLong("mall_id"),
				rs.getLong("plus_free"),
				rs.getLong("plus_pay"),
				rs.getLong("minus_free"),
				rs.getLong("minus_play")));

		return reader;
	}*/

	@Bean
	public JdbcBatchItemWriter<BillingStat> writer2() {
		JdbcBatchItemWriter<BillingStat> writer = new JdbcBatchItemWriter<BillingStat>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<BillingStat>());
		writer.setSql("INSERT INTO `mall_billing_stat` (`date`, `mall_id`, `cash_plus_free`, `cash_plus_pay`, `cash_minus_free`, `cash_minus_pay`, `is_stat_record`) " +
						" VALUES " +
						"   (:date, :mall_id, :cash_plus_free, :cash_plus_pay, :cash_minus_free, :cash_minus_pay, 1) " +
						" ON DUPLICATE KEY UPDATE " +
						"   cash_plus_free = :cash_plus_free, " +
						"   cash_plus_pay = :cash_plus_pay, " +
						"   cash_minus_free = :cash_minus_free, " +
						"   cash_minus_pay = :cash_minus_pay, " +
						"   is_stat_record = 1 " +
						" ");
		writer.setDataSource(dataSource);

		return writer;
	}



}
