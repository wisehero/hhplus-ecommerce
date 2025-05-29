package kr.hhplus.be.server.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaTopicConfig {
	@Bean
	public NewTopic orderDataTopic() {
		String topicName = "order-data";
		log.info("토픽 생성 설정: {}", topicName);
		return TopicBuilder.name(topicName)
			.partitions(3)
			.replicas(1)
			.build();
	}

	@Bean
	public NewTopic couponIssueTopic() {
		String topicName = "coupon-issue";
		log.info("쿠폰 발급 토픽 생성 설정: {}", topicName);
		return TopicBuilder.name(topicName)
			.partitions(1)  // 선착순 보장을 위해 단일 파티션
			.replicas(1)
			.build();
	}
}
