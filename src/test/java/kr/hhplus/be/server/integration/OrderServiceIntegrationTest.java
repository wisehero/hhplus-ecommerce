package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class OrderServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("주문을 주문 상품과 함께 저장한다.")
	void shouldSaveOrderWithOrderProducts() {
		// given
		User user = Instancio.of(User.class)
			.ignore(field(User.class, "id"))
			.create();

		Product product1 = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "price"), BigDecimal.valueOf(5000))
			.set(field(Product.class, "stock"), 10L)
			.create();

		Product product2 = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "price"), BigDecimal.valueOf(10000))
			.set(field(Product.class, "stock"), 10L)
			.create();

		Order order = Order.create(user);
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());

		order.addOrderProduct(product1, 1L);
		order.addOrderProduct(product2, 2L);

		// when
		Order savedOrder = orderService.order(order);

		// then
		Order findOrder = orderRepository.findOrderById(savedOrder.getId());
		assertAll(
			() -> assertThat(findOrder.getOrderProducts()).hasSize(2),
			() -> assertThat(findOrder.getTotalPrice()).isEqualByComparingTo("25000")
		);
	}

	@Test
	@DisplayName("주문 상태가 PENDING일 때 completeOrder를 호출하면 상태가 PAID로 변경된다.")
	void shouldChangeOrderStatusToPaidWhenCompleted() {
		// given
		User user = Instancio.of(User.class)
			.ignore(field(User.class, "id"))
			.create();

		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "price"), BigDecimal.valueOf(10000))
			.set(field(Product.class, "stock"), 10L)
			.create();

		Order order = Order.create(user);
		order.addOrderProduct(product, 1L);
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());

		order = orderRepository.save(order);

		// when
		orderService.completeOrder(order);

		// then
		Order updatedOrder = orderRepository.findOrderById(order.getId());

		assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	@DisplayName("주문 상태가 PENDING일 때 expireOrder를 호출하면 상태가 EXPIRED로 변경된다.")
	void shouldChangeOrderStatusToExpired() {
		// given
		User user = Instancio.of(User.class)
			.ignore(field(User.class, "id"))
			.create();

		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.set(field(Product.class, "price"), BigDecimal.valueOf(8000))
			.set(field(Product.class, "stock"), 10L)
			.create();

		Order order = Order.create(user);
		order.addOrderProduct(product, 1L);
		ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
		ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());

		order = orderRepository.save(order); // 초기 상태는 PENDING

		// when
		Order expiredOrder = orderService.expireOrder(order);

		// then
		Order findOrder = orderRepository.findOrderById(expiredOrder.getId());

		assertThat(findOrder.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
	}

	@Test
	@DisplayName("orderdAt이 데드라인 이전이고 상태가 PENDING인 주문만 조회된다.")
	void shouldFilterOnlyPendingAndOverdueOrders() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime deadLine = now.plusMinutes(5);

		List<Order> orders = IntStream.range(0, 5)
			.mapToObj(i -> {
				Product product = Instancio.of(Product.class)
					.ignore(field(Product.class, "id"))
					.create();
				OrderProduct orderProduct = OrderProduct.create(product, 1L);

				return Instancio.of(Order.class)
					.ignore(field(Order.class, "id"))
					.set(field(Order.class, "orderStatus"), OrderStatus.PENDING)
					.supply(field(Order.class, "orderProducts"), () -> List.of(orderProduct))
					.set(field(Order.class, "orderedAt"), now.minusMinutes(5 + i))
					.create();
			})
			.toList();

		orders.forEach(orderRepository::save);

		// when
		List<Order> result = orderService.getOverDueOrders(deadLine);

		// then
		assertThat(result)
			.allSatisfy(order -> {
				assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
				assertThat(order.getOrderedAt()).isBefore(deadLine);
			});
	}

	@Test
	@DisplayName("유효한 주문 ID로 주문을 조회하면 주문과 주문상품이 반환된다")
	void shouldReturnOrderWithOrderProductsWhenIdIsValid() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();

		OrderProduct orderProduct = OrderProduct.create(product, 1L);

		Order order = Instancio.of(Order.class)
			.ignore(field(Order.class, "id"))
			.set(field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.supply(field(Order.class, "orderProducts"), () -> List.of(orderProduct))
			.create();

		Order savedOrder = orderRepository.save(order);

		// when
		Order result = orderService.getOrderById(savedOrder.getId());

		// then
		assertAll(
			() -> assertThat(result.getId()).isEqualTo(savedOrder.getId()),
			() -> assertThat(result.getOrderProducts()).hasSize(1),
			() -> assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING)
		);
	}

	@Test
	@DisplayName("주문 ID가 null이면 IllegalArgumentException가 발생한다")
	void shouldThrowExceptionWhenOrderIdIsNull() {
		// when & then
		assertThatThrownBy(() -> orderService.getOrderById(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("주문 ID는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("유효한 orderId로 주문 상품을 조회하면 주문한 상품 목록이 반환된다")
	void shouldReturnOrderProductsWhenOrderIdIsValid() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();

		OrderProduct orderProduct = OrderProduct.create(product, 2L);

		Order order = Instancio.of(Order.class)
			.ignore(field(Order.class, "id"))
			.set(field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.supply(field(Order.class, "orderProducts"), () -> List.of(orderProduct))
			.create();

		Order savedOrder = orderRepository.save(order);

		// when
		List<OrderProduct> orderProducts = orderService.getOrderProducts(savedOrder.getId());

		// then
		assertAll(
			() -> assertThat(orderProducts).hasSize(1),
			() -> assertThat(orderProducts.get(0).getProductId()).isEqualTo(product.getId()),
			() -> assertThat(orderProducts.get(0).getQuantity()).isEqualTo(2L)
		);
	}

	@Test
	@DisplayName("orderId로 주문한 상품들을 가져올 때, orderId가 null이면 IllegalArgumentException 발생한다")
	void shouldThrowExceptionWhenOrderIdIsNullIfGetOrderProducts() {
		// when & then
		assertThatThrownBy(() -> orderService.getOrderProducts(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("주문 ID는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("현재로부터 59분 이내에 결제 완료 주문 조회 주문이 1건 있을 경우 조회된다")
	void shouldReturnSingleRecentPaidOrder() {
		// given
		LocalDateTime fixedNow = LocalDateTime.now();

		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();
		productRepository.save(product);

		OrderProduct orderProduct = OrderProduct.create(product, 2L);

		Order order = Instancio.of(Order.class)
			.ignore(field(Order.class, "id"))
			.set(field(Order.class, "orderStatus"), OrderStatus.PAID)
			.set(field(Order.class, "orderedAt"), fixedNow.minusMinutes(1))
			.supply(field(Order.class, "orderProducts"), () -> List.of(orderProduct))
			.create();

		orderRepository.save(order);

		// when
		List<Order> result = orderService.getPaidOrdersWithinOneHour(fixedNow);

		// then
		assertAll(
			() -> assertThat(result).hasSize(1),
			() -> assertThat(result.get(0).getId()).isEqualTo(order.getId())
		);
	}

	@Test
	@DisplayName("현재로부터 60분 이전 이면 조회되지 않는다")
	void shouldNotReturnIfCreatedBeforeOneHour() {
		// given
		LocalDateTime fixedNow = LocalDateTime.now();

		Product product = productRepository.save(
			Instancio.of(Product.class).ignore(field(Product.class, "id")).create());

		Order order = Instancio.of(Order.class)
			.ignore(field(Order.class, "id"))
			.set(field(Order.class, "orderStatus"), OrderStatus.PAID)
			.set(field(Order.class, "orderedAt"), fixedNow.minusMinutes(61))
			.supply(field(Order.class, "orderProducts"), () -> List.of(OrderProduct.create(product, 1L)))
			.create();

		orderRepository.save(order);

		// when
		List<Order> result = orderService.getPaidOrdersWithinOneHour(fixedNow);

		// then
		assertThat(result).isEmpty();
	}
}
