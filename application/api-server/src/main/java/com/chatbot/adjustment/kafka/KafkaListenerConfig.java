package com.chatbot.adjustment.kafka;

import com.chatbot.adjustment.kafka.domain.CdrUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaListenerConfig {

	@Autowired
	private KafkaConsumerProperties kafkaConsumerProperties;

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CdrUnit> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CdrUnit> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConcurrency(1);
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}

	@Bean
	public ConsumerFactory<String, CdrUnit> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerProps(), stringKeyDeserializer(), cdrUnitJsonValueDeserializer());
	}


	@Bean
	public Map<String, Object> consumerProps() {
		Map<String, Object> props = new HashMap<>();

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getBootstrap());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroup());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); //자동커밋정책
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100"); //자동오프셋 커밋 주기
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000"); //세션타임아웃
		return props;
	}

	@Bean
	public Deserializer stringKeyDeserializer() {
		return new StringDeserializer();
	}

	@Bean
	public Deserializer cdrUnitJsonValueDeserializer() {
		return new JsonDeserializer(CdrUnit.class);
	}
}