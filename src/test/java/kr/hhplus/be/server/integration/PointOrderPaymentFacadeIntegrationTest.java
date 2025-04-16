package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.point.PointOrderPaymentFacade;
import kr.hhplus.be.server.application.point.dto.PointOrderPaymentCommand;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.order.client.DataPlatformClient;
import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class PointOrderPaymentFacadeIntegrationTest extends IntgerationTestSupport {

	@Autowired
	PointOrderPaymentFacade pointOrderPaymentFacade;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@MockitoBean
	private DataPlatformClient dataPlatformClient;

	@Test
	@DisplayName("유저 포인트로 결제 시 주문 상태가 PAID로 변경되고 포인트가 차감되며 외부 시스템 전송이 발생한다.")
	void shouldPayOrderWithPointsSuccessfully() {
		// given
		User user = userRepository.save(Instancio.of(User.class)
			.ignore(field("id"))
			.create());

		pointRepository.save(Point.create(user.getId(), Balance.createBalance(BigDecimal.valueOf(100_000))));

		Product product = productRepository.save(Instancio.of(Product.class)
			.ignore(field("id"))
			.set(field("price"), BigDecimal.valueOf(30000))
			.set(field("stock"), 50L)
			.create());

		Order order = Order.create(user);
		order.addOrderProduct(product, 2L); // 총액: 60,000
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());

		orderRepository.save(order);

		PointOrderPaymentCommand command = new PointOrderPaymentCommand(user.getId(), order.getId());

		// when
		pointOrderPaymentFacade.pointPayment(command);

		// then
		Order updatedOrder = orderRepository.findOrderById(order.getId());
		Point userPoint = pointRepository.findByUserId(user.getId());

		assertAll(
			() -> assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID),
			() -> assertThat(userPoint.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40_000)),
			// 100_000 - 60_000
			() -> verify(dataPlatformClient).send(any(Order.class))
		);
	}
}
