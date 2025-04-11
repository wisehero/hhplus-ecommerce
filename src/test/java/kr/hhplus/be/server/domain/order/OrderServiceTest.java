package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBePaidException;
import kr.hhplus.be.server.domain.product.Product;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderProductRepository orderProductRepository;

	@InjectMocks
	private OrderService orderService;

	@Test
	@DisplayName("주문을 생성하면 저장된 주문과 주문상품이 반환된다.")
	void createOrder() {
		// given
		Long userId = 1L;
		Long userCouponId = null;
		BigDecimal totalPrice;

		Product product1 = Product.create("상품 A", "설명", BigDecimal.valueOf(1000), 100L);
		Product product2 = Product.create("상품 B", "설명", BigDecimal.valueOf(2000), 50L);

		OrderProduct op1 = OrderProduct.create(product1, 2L);
		OrderProduct op2 = OrderProduct.create(product2, 1L);

		List<OrderProduct> orderProducts = List.of(op1, op2);
		totalPrice = op1.getAmount().add(op2.getAmount());

		Order savedOrder = Order.create(userId, userCouponId, totalPrice);
		ReflectionTestUtils.setField(savedOrder, "id", 100L);

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		// when
		Order result = orderService.order(userId, userCouponId, orderProducts, totalPrice);

		// then
		assertAll(
			() -> assertThat(result.getId()).isEqualTo(100L),
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getUserCouponId()).isNull()
		);

		// OrderProduct에 orderId가 잘 지정되었는지 검증
		assertThat(orderProducts).allSatisfy(op ->
			assertThat(op.getOrderId()).isEqualTo(100L)
		);

		verify(orderRepository).save(any(Order.class));
		verify(orderProductRepository).saveAll(orderProducts);
	}

	@Test
	@DisplayName("주문 대기 상품을 완료 상태(PAID)로 변경한다")
	void completeOrder() {
		// given
		Long orderId = 1L;

		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));
		ReflectionTestUtils.setField(order, "id", orderId);

		when(orderRepository.findById(orderId)).thenReturn(order);

		// when
		orderService.completeOrder(orderId);

		// then
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
		verify(orderRepository).findById(orderId);
	}

	@Test
	@DisplayName("이미 결제 완료된 주문은 결제 완료 처리할 수 없다")
	void completeOrderFailsIfAlreadyPaid() {
		// given
		Order paidOrder = Order.create(1L, null, BigDecimal.valueOf(10000));
		paidOrder.paid(); // 이미 결제 완료
		Long orderId = 1L;
		ReflectionTestUtils.setField(paidOrder, "id", orderId);

		when(orderRepository.findById(orderId)).thenReturn(paidOrder);

		// expect
		assertThatThrownBy(() -> orderService.completeOrder(orderId))
			.isInstanceOf(OrderCannotBePaidException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("PENDING 상태의 주문만 결제할 수 있습니다.");
	}

	@Test
	@DisplayName("PENDING 상태의 주문은 만료시 EXPIRED 상태로 변경된다")
	void expireOrder() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));
		ReflectionTestUtils.setField(order, "id", 1L);

		when(orderRepository.findById(1L)).thenReturn(order);

		// when
		Order result = orderService.expireOrder(1L);

		// then
		assertAll(
			() -> assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED),
			() -> assertThat(result).isSameAs(order)
		);

		verify(orderRepository).findById(1L);
	}

	@Test
	@DisplayName("PAID 상태인 주문은 만료시 OrderCannotBeExpiredException 예외가 발생한다")
	void expireOrderFailWhenAlreadyPaid() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));
		order.paid(); // 상태를 PAID로 만듦
		ReflectionTestUtils.setField(order, "id", 2L);

		when(orderRepository.findById(2L)).thenReturn(order);

		// expect
		assertThatThrownBy(() -> orderService.expireOrder(2L))
			.isInstanceOf(OrderCannotBeExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("결제 완료된 주문은 만료할 수 없습니다.");

		verify(orderRepository).findById(2L);
	}

	@Test
	@DisplayName("주문 ID로 주문을 조회한다")
	void getOrderById_returnsOrder() {
		// given
		Long orderId = 1L;
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));

		when(orderRepository.findById(orderId)).thenReturn(order);

		// when
		Order result = orderService.getOrderById(orderId);

		// then
		assertThat(result).isEqualTo(order);
		verify(orderRepository).findById(orderId);
	}

	@Test
	@DisplayName("주문 ID로 해당 주문의 주문상품 목록을 조회한다")
	void getOrderProducts_returnsOrderProducts() {
		// given
		Long orderId = 1L;

		OrderProduct op1 = OrderProduct.builder()
			.orderId(orderId)
			.productId(10L)
			.quantity(2L)
			.amount(BigDecimal.valueOf(2000))
			.build();

		OrderProduct op2 = OrderProduct.builder()
			.orderId(orderId)
			.productId(20L)
			.quantity(1L)
			.amount(BigDecimal.valueOf(3000))
			.build();

		List<OrderProduct> orderProducts = List.of(op1, op2);

		when(orderProductRepository.findByOrderId(orderId)).thenReturn(orderProducts);

		// when
		List<OrderProduct> result = orderService.getOrderProducts(orderId);

		// then
		assertAll(
			() -> assertThat(result).hasSize(2),
			() -> assertThat(result).containsExactly(op1, op2)
		);

		verify(orderProductRepository).findByOrderId(orderId);
	}

	@Test
	@DisplayName("마감 시각 이전에 생성된 PENDING 상태의 주문들을 조회한다")
	void getOverDueOrders_returnsPendingOrdersBeforeDeadline() {
		// given
		LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);

		Order order1 = Order.create(1L, null, BigDecimal.valueOf(10000));
		Order order2 = Order.create(2L, null, BigDecimal.valueOf(20000));

		// createdAt은 BaseTimeEntity에 있지만, 테스트에서는 리플렉션으로 세팅 필요
		ReflectionTestUtils.setField(order1, "createdAt", deadline.minusMinutes(1));
		ReflectionTestUtils.setField(order2, "createdAt", deadline.minusMinutes(2));

		List<Order> pendingOrders = List.of(order1, order2);

		when(orderRepository.findAllPendingBefore(OrderStatus.PENDING, deadline))
			.thenReturn(pendingOrders);

		// when
		List<Order> result = orderService.getOverDueOrderIds(deadline);

		// then
		assertAll(
			() -> assertThat(result).hasSize(2),
			() -> assertThat(result).containsExactlyElementsOf(pendingOrders),
			() -> assertThat(result)
				.allSatisfy(order -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING))
		);

		verify(orderRepository).findAllPendingBefore(OrderStatus.PENDING, deadline);
	}
}