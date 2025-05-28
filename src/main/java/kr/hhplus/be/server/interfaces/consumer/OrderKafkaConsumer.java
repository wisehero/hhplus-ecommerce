package kr.hhplus.be.server.interfaces.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaConsumer {

	private final DataPlatformClient dataPlatformClient;

	private static final String TOPIC_ORDER_DATA = "order-data";

	@KafkaListener(topics = TOPIC_ORDER_DATA)
	public void consume(OrderInfo orderInfo, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
		@Header(KafkaHeaders.OFFSET) long offset) {

		try {
			log.info("주문 정보 수신: orderId={}, topic={}, partition={}, offset={}",
				orderInfo.orderId(), topic, partition, offset);

			dataPlatformClient.send(orderInfo);

			log.info("주문 정보 처리 완료: orderId={}", orderInfo.orderId());
		} catch (Exception e) {
			log.error("주문 정보 처리 실패: orderId={}", orderInfo.orderId(), e);
			handleProcessingFailure(orderInfo, e);
		}

	}

	private void handleProcessingFailure(OrderInfo orderInfo, Exception e) {
		// 실패 처리 로직(DB에 기록하거나 알림 발송하는 걸로 발전시켜보자)
		log.warn("주문 처리 실패 - 추가 처리 필요: orderId={}", orderInfo.orderId());
	}
}
