package kr.hhplus.be.server.interfaces.consumer;

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

import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderKafkaConsumerTest {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockitoSpyBean
	private OrderKafkaConsumer orderKafkaConsumer;

	@MockitoSpyBean
	private DataPlatformClient dataPlatformClient;

	@Test
	@DisplayName("카프카 메시지 수신 시 컨슈머가 정상적으로 처리한다")
	void shouldConsumeOrderInfoFromKafka() throws InterruptedException {
		// given
		OrderInfo orderInfo = Instancio.of(OrderInfo.class).create();

		// when - 카프카에 메시지 전송
		kafkaTemplate.send("order-data", orderInfo.orderId().toString(), orderInfo);

		// then - 컨슈머가 메시지를 받아서 처리했는지 검증
		await().atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> {
				verify(orderKafkaConsumer).consume(
					eq(orderInfo),
					eq("order-data"),
					anyInt(),
					anyLong()
				);
			});

		// DataPlatformClient도 호출되었는지 검증
		await().atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> {
				verify(dataPlatformClient).send(eq(orderInfo));
			});
	}
}