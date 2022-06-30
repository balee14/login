package com.chatbot.login.config;

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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"com.chatbot.login.homepage.repository"},
		entityManagerFactoryRef = "homepageEntityManagerFactory",
		transactionManagerRef = "homepageTransactionManager"
)
public class HomepageDbConfig {

	private static final String DEFAULT_NAMING_STRATEGY = "org.springframework.boot.orm.jpa.hibernate.SpringNamingStrategy";

	@Bean(name = "homepageDataSource")
	@ConfigurationProperties(prefix = "homepage.datasource")
	public DataSource dataSource() {

		return DataSourceBuilder.create().build();
	}

	@Bean(name="homepageEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {

		Map<String, String> propertiesHashMap = new HashMap<>();
		propertiesHashMap.put("hibernate.ejb.naming_strategy",DEFAULT_NAMING_STRATEGY);

		return builder
				.dataSource(dataSource())
				.packages("com.chatbot.login.homepage.domain")
				.properties(propertiesHashMap)
				.build();
	}

	@Bean(name = "homepageTransactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(entityManagerFactory(builder).getObject());
	}
}
