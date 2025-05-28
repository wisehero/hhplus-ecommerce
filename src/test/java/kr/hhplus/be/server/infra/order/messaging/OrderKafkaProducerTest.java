package kr.hhplus.be.server.infra.order.messaging;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import kr.hhplus.be.server.domain.order.dto.OrderInfo;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OrderKafkaProducerTest {

	@Autowired
	private OrderKafkaProducer orderKafkaProducer;

	@MockitoSpyBean
	private KafkaTemplate<String, Object> kafkaTemplate;


	@Test
	@DisplayName("주문 정보 카프카 전송 테스트")
	void shouldSendOrderInfoToKafka() {
		// given
		OrderInfo orderInfo = Instancio.of(OrderInfo.class)
			.create();

		// when then
		assertDoesNotThrow(() -> {
			orderKafkaProducer.send(orderInfo);
		});

		await().atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> {
				verify(kafkaTemplate).send(eq("order-data"), anyString(), eq(orderInfo));
			});
	}

}