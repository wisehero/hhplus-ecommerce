package kr.hhplus.be.server.application.point;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointOrderPaymentFacade {

	private final PointService pointService;
	private final OrderService orderService;
	private final DataPlatformClient dataPlatformClient;

	@Transactional
	public void pointPayment(PointOrderPaymentCommand command) {
		// 결제할 주문 건 가져오기
		Order order = orderService.getOrderById(command.orderId());

		// 결제
		PointUseCommand pointUseCommand = PointUseCommand.of(command.userId(), order.getTotalPrice());
		pointService.useUserPoint(pointUseCommand);

		// 주문 상태 PENDING -> PAID
		orderService.completeOrder(order);

		dataPlatformClient.send(order);
	}
}
