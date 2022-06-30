package com.chatbot.adjustment;

import com.chatbot.adjustment.kafka.KafkaConsumerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class AdjustmentApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AdjustmentApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AdjustmentApplication.class, args);
	}
}