package kr.hhplus.be.server.config.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		log.info("KafkaTemplate Bean 생성 중...");
		return new KafkaTemplate<>(producerFactory);
	}
}
