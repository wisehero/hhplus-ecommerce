package kr.hhplus.be.server.application.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.point.PointService;

@ExtendWith(MockitoExtension.class)
class PointOrderPaymentFacadeTest {

	@Mock
	private PointService pointService;

	@Mock
	private OrderService orderService;

	@Mock
	private DataPlatformClient dataPlatformClient;

	@InjectMocks
	private PointOrderPaymentFacade pointOrderPaymentFacade;

	@Test
	@DisplayName("포인트 결제를 수행하면 포인트 차감, 주문 완료, 전송이 순차적으로 이루어진다")
	void pointPaymentSuccessfullyProcessesOrder() {
		// given
		Long userId = 1L;
		Long orderId = 10L;
		BigDecimal totalPrice = BigDecimal.valueOf(5000);

		Order order = Order.create(userId, null, totalPrice);
		ReflectionTestUtils.setField(order, "id", orderId);

		PointOrderPaymentCommand command = new PointOrderPaymentCommand(orderId, userId);

		when(orderService.getOrderById(orderId)).thenReturn(order);

		// when
		pointOrderPaymentFacade.pointPayment(command);

		// then
		assertAll(
			() -> verify(orderService).getOrderById(orderId),
			() -> verify(pointService).useUserPoint(userId, totalPrice),
			() -> verify(orderService).completeOrder(orderId),
			() -> verify(dataPlatformClient).send(order)
		);
	}
}