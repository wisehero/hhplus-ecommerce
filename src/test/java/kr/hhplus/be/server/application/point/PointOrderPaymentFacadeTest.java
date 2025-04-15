package kr.hhplus.be.server.application.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointUseCommand;

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
	@DisplayName("포인트 결제 시 주문 조회, 포인트 차감, 주문 완료 처리 및 외부 시스템 전송이 수행된다.")
	void shouldCompletePointPaymentSuccessfully() {
		// given
		Long orderId = 1L;
		Long userId = 10L;
		BigDecimal orderTotalPrice = BigDecimal.valueOf(20000);

		PointOrderPaymentCommand command = new PointOrderPaymentCommand(orderId, userId);

		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "id"), orderId)
			.set(Select.field(Order.class, "totalPrice"), orderTotalPrice)
			.create();

		when(orderService.getOrderById(orderId)).thenReturn(order);

		// when
		pointOrderPaymentFacade.pointPayment(command);

		// then
		PointUseCommand expectedPointUseCommand = PointUseCommand.of(userId, orderTotalPrice);

		assertAll(
			() -> verify(orderService).getOrderById(orderId),
			() -> verify(pointService).useUserPoint(expectedPointUseCommand),
			() -> verify(orderService).completeOrder(order),
			() -> verify(dataPlatformClient).send(order)
		);
	}
}