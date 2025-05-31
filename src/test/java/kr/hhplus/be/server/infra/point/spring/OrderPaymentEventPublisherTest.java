package kr.hhplus.be.server.infra.point.spring;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import kr.hhplus.be.server.domain.point.event.type.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.user.User;

@SpringBootTest
@RecordApplicationEvents
class PointPaymentEventPublisherTest {

	@MockitoSpyBean
	private PointPaymentEventPublisher eventPublisher;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Test
	@DisplayName("결제 완료 이벤트 발행시, ApplicationEventPublisher를 통해 이벤트가 발행된다")
	void shouldPublishEventThroughApplicationEventPublisher() {
		// given
		Order order = Order.create(User.create(1L));
		OrderInfo orderInfo = new OrderInfo(order);
		PaymentSuccessEvent event = new PaymentSuccessEvent(orderInfo);

		// when
		eventPublisher.publish(event);

		// then
		verify(eventPublisher).publish(any(PaymentSuccessEvent.class));
		assertThat(applicationEvents.stream(PaymentSuccessEvent.class).count()).isEqualTo(1);

		PaymentSuccessEvent publishedEvent = applicationEvents.stream(PaymentSuccessEvent.class)
			.findFirst()
			.orElseThrow();

		assertThat(publishedEvent.getOrderInfo()).isEqualTo(orderInfo);
	}
}