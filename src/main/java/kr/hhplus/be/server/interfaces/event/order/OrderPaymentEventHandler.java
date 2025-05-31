package kr.hhplus.be.server.interfaces.event.order;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.server.domain.point.event.type.PaymentSuccessEvent;
import kr.hhplus.be.server.infra.order.messaging.OrderKafkaProducer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderPaymentEventHandler {

	private final OrderKafkaProducer orderKafkaProducer;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 기본 값은 AFTER_COMMIT
	public void handleOrderPaymentEvent(PaymentSuccessEvent event) {
		// 주문 결제 완료 후 데이터 플랫폼에 전송
		orderKafkaProducer.send(event.getOrderInfo());
	}

}
