package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBePaidException;
import kr.hhplus.be.server.domain.product.Product;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	@Test
	@DisplayName("Order가 저장되면 Repository의 save 메서드가 호출되고 저장된 결과가 반환된다.")
	void shouldSaveOrderAndReturnSavedInstance() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "id"), null)
			.create();

		Order savedOrder = Instancio.of(Order.class)
			.set(Select.field(Order.class, "id"), 123L)
			.create();

		when(orderRepository.save(order)).thenReturn(savedOrder);

		// when
		Order result = orderService.order(order);

		// then
		assertAll(
			() -> verify(orderRepository, times(1)).save(order),
			() -> assertThat(result).isEqualTo(savedOrder)
		);
	}

	@Test
	@DisplayName("PENDING 상태의 주문을 결제 완료 상태로 변경한다.")
	void shouldCompleteOrderAndChangeStatusToPaid() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.create();

		when(orderRepository.save(order)).thenReturn(order);

		// when
		orderService.completeOrder(order);

		// then
		assertAll(
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID),
			() -> verify(orderRepository).save(order)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"PAID", "EXPIRED"})
	@DisplayName("PENDING이 아닌 상태의 주문을 완료하려 하면 예외가 발생하고 저장되지 않는다.")
	void shouldThrowIfOrderStatusNotPending(String status) {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.valueOf(status))
			.create();

		// when & then
		assertThatThrownBy(() -> orderService.completeOrder(order))
			.isInstanceOf(OrderCannotBePaidException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("PENDING 상태의 주문만 결제할 수 있습니다.");

		verify(orderRepository, never()).save(any());
	}

	@Test
	@DisplayName("PENDING 상태의 주문을 expireOrder로 만료하면 상태가 EXPIRED로 변경되고 저장된다.")
	void shouldExpireOrderAndSaveWhenStatusIsPending() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.create();

		Order expiredOrder = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.EXPIRED)
			.create();

		when(orderRepository.save(order)).thenReturn(expiredOrder);

		// when
		Order result = orderService.expireOrder(order);

		// then
		assertAll(
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED),
			() -> assertThat(result).isEqualTo(expiredOrder),
			() -> verify(orderRepository).save(order)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"PAID", "EXPIRED"})
	@DisplayName("PENDING이 아닌 상태의 주문을 만료하려고 하면 예외가 발생하고 저장되지 않는다.")
	void shouldThrowExceptionWhenOrderStatusIsNotPending(String status) {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.valueOf(status))
			.create();

		// when & then
		assertThatThrownBy(() -> orderService.expireOrder(order))
			.isInstanceOf(OrderCannotBeExpiredException.class)
			.extracting("detail")
			.isEqualTo("결제 완료된 주문은 만료할 수 없습니다.");

		verify(orderRepository, never()).save(any());
	}

	@Test
	@DisplayName("주문 ID로 주문을 조회하면 해당 주문이 반환된다.")
	void shouldReturnOrderWhenIdIsValid() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "id"), 123L)
			.create();

		when(orderRepository.findOrderById(123L)).thenReturn(order);

		// when
		Order result = orderService.getOrderById(123L);

		// then
		assertAll(
			() -> assertThat(result).isNotNull(),
			() -> assertThat(result.getId()).isEqualTo(123L),
			() -> verify(orderRepository).findOrderById(123L)
		);
	}

	@Test
	@DisplayName("orderId가 null이면 예외가 발생한다.")
	void shouldThrowExceptionWhenOrderIdIsNull() {
		// when & then
		assertThatThrownBy(() -> orderService.getOrderById(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("주문 ID는 null일 수 없습니다."); // 메시지는 너의 구현에 맞게

		verify(orderRepository, never()).findOrderById(any());
	}

	@Test
	@DisplayName("orderId가 주어지면 해당 주문의 주문 상품 목록을 조회한다.")
	void shouldReturnOrderProductsWhenOrderIdIsValid() {
		// given
		Long orderId = 123L;

		Product product1 = Instancio.of(Product.class)
			.set(Select.field(Product.class, "id"), 1L)
			.set(Select.field(Product.class, "productName"), "상품A")
			.set(Select.field(Product.class, "price"), BigDecimal.valueOf(10000))
			.create();

		Product product2 = Instancio.of(Product.class)
			.set(Select.field(Product.class, "id"), 2L)
			.set(Select.field(Product.class, "productName"), "상품B")
			.set(Select.field(Product.class, "price"), BigDecimal.valueOf(20000))
			.create();

		OrderProduct op1 = OrderProduct.create(product1, 2L);
		OrderProduct op2 = OrderProduct.create(product2, 1L);

		// assignOrderId 는 도메인 내부에서 따로 처리되므로 테스트에서 직접 세팅
		op1.assignOrderId(orderId);
		op2.assignOrderId(orderId);

		List<OrderProduct> orderProducts = List.of(op1, op2);

		when(orderRepository.findOrderProductsByOrderId(orderId)).thenReturn(orderProducts);

		// when
		List<OrderProduct> result = orderService.getOrderProducts(orderId);

		// then
		assertAll(
			() -> assertThat(result).hasSize(2),
			() -> assertThat(result).extracting(OrderProduct::getOrderId).containsOnly(orderId),
			() -> assertThat(result).extracting(OrderProduct::getProductName).containsExactly("상품A", "상품B"),
			() -> verify(orderRepository).findOrderProductsByOrderId(orderId)
		);
	}

	@Test
	@DisplayName("특정 orderId에 포함된 상품 조회시 orderId가 null이면 IllegalArgumentException이 발생한다.")
	void shouldFailWhenFetchingOrderProductsWithNullOrderId () {
		// when & then
		assertThatThrownBy(() -> orderService.getOrderProducts(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("주문 ID는 null일 수 없습니다.");

		verify(orderRepository, never()).findOrderProductsByOrderId(any());
	}

	@Test
	@DisplayName("마감 기한 이전에 생성된 PENDING 주문만 조회된다.")
	void shouldReturnOnlyPendingOrdersCreatedBeforeDeadline() {
		// given
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 15, 12, 0, 0);
		LocalDateTime deadline = fixedNow.minusMinutes(5); // 11:55

		Order overdueOrder = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.set(Select.field(BaseTimeEntity.class, "createdAt"), fixedNow.minusMinutes(10)) // 11:50
			.create();

		Order recentOrder = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.set(Select.field(BaseTimeEntity.class, "createdAt"), fixedNow.minusMinutes(2)) // 11:58
			.create();

		when(orderRepository.findAllPendingBefore(OrderStatus.PENDING, deadline))
			.thenReturn(List.of(overdueOrder));

		// when
		List<Order> result = orderService.getOverDueOrderIds(deadline);

		// then
		assertAll(
			() -> assertThat(result).hasSize(1),
			() -> assertThat(result.get(0).getCreatedAt()).isBefore(deadline),
			() -> verify(orderRepository).findAllPendingBefore(OrderStatus.PENDING, deadline)
		);
	}

	@Test
	@DisplayName("마감 기한이 null이면 IllegalArgumentException이 발생한다.")
	void fetchOverdueOrdersFailsWhenDeadlineIsNull() {
		// when & then
		assertThatThrownBy(() -> orderService.getOverDueOrderIds(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("마감 기한은 null일 수 없습니다.");

		verify(orderRepository, never()).findAllPendingBefore(any(), any());
	}
}