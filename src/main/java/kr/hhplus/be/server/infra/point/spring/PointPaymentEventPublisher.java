package kr.hhplus.be.server.infra.point.spring;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.point.event.PointEventPublisher;
import kr.hhplus.be.server.domain.point.event.type.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PointPaymentEventPublisher implements PointEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	public void publish(PaymentSuccessEvent event) {
		eventPublisher.publishEvent(event);
	}
}
