package kr.hhplus.be.server.application.point;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.dto.OrderInfo;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import kr.hhplus.be.server.domain.point.event.type.PaymentSuccessEvent;
import kr.hhplus.be.server.infra.point.spring.PointPaymentEventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointOrderPaymentFacade {

	private final PointService pointService;
	private final OrderService orderService;
	private final PointPaymentEventPublisher eventPublisher;

	@Transactional
	public void pointPayment(PointOrderPaymentCommand command) {
		// 결제할 주문 건 가져오기
		Order order = orderService.getOrderById(command.orderId());

		// 결제
		PointUseCommand pointUseCommand = PointUseCommand.of(
			command.userId(),
			order.getDiscountedPrice().compareTo(BigDecimal.ZERO) == 0 ? order.getTotalPrice() :
				order.getDiscountedPrice());
		pointService.useUserPointV2(pointUseCommand);

		// 주문 상태 PENDING -> PAID
		orderService.completeOrder(order);

		eventPublisher.publish(new PaymentSuccessEvent(new OrderInfo(order)));
	}
}
