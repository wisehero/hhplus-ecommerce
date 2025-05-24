package kr.hhplus.be.server.domain.point.event;

import kr.hhplus.be.server.domain.point.event.type.PaymentSuccessEvent;

public interface PointEventPublisher {

	void publish(PaymentSuccessEvent event);
}
