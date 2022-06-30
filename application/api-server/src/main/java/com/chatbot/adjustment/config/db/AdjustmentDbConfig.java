package com.chatbot.adjustment.config.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.chatbot.adjustment.web.adjustment.repository"},
		entityManagerFactoryRef = "adjustmentEntityManagerFactory",
		transactionManagerRef = "adjustmentTransactionManager"
)
public class AdjustmentDbConfig {

	private static final String DEFAULT_NAMING_STRATEGY = "org.springframework.boot.orm.jpa.hibernate.SpringNamingStrategy";

	@Bean(name = "adjustmentDataSource")
	@ConfigurationProperties(prefix = "adjustment.datasource")
	public DataSource dataSource() {

		return DataSourceBuilder.create().build();
	}

	@Bean(name="adjustmentEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {

		Map<String, String> propertiesHashMap = new HashMap<>();
		propertiesHashMap.put("hibernate.ejb.naming_strategy",DEFAULT_NAMING_STRATEGY);
		propertiesHashMap.put("hibernate.id.new_generator_mappings","false");

		return builder
				.dataSource(dataSource())
				.packages("com.chatbot.adjustment.web.adjustment.domain")
				.properties(propertiesHashMap)
				.build();
	}

	@Bean(name = "adjustmentTransactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(entityManagerFactory(builder).getObject());
	}
}
