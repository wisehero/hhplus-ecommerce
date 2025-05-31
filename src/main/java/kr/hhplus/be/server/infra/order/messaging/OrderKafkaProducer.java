package kr.hhplus.be.server.infra.order.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String TOPIC_ORDER_DATA = "order-data";

	public void send(OrderInfo orderInfo) {
		try {
			log.info("주문 정보 카프카 전송 시작: orderId={}", orderInfo.orderId());
			kafkaTemplate.send(TOPIC_ORDER_DATA, orderInfo.orderId().toString(), orderInfo)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("주문 정보 카프카 전송 성공: orderId={}, offset={}, partition={}",
							orderInfo.orderId(),
							result.getRecordMetadata().offset(),
							result.getRecordMetadata().partition());
					} else {
						log.error("주문 정보 카프카 전송 실패: orderId={}", orderInfo.orderId(), ex);
						handleSendFailure(orderInfo, ex);
					}
				});
		} catch (Exception e) {
			log.error("카프카 메시지 발행 중 예외 발생: orderId={}", orderInfo.orderId(), e);
			throw new RuntimeException("카프카 메시지 전송 실패", e);
		}
	}

	private void handleSendFailure(OrderInfo orderInfo, Throwable ex) {
		// 여기서 메세지 전송 실패에 대한 추가 로직을 작성할 수 있을 것 같다.
		log.warn("메시지 전송 실패 처리 필요: orderId={}", orderInfo.orderId());
	}
}
