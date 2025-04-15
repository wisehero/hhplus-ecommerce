package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.order.exception.OrderAlreadyCouponAppliedException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBePaidException;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;

class OrderTest {

	@Test
	@DisplayName("User가 주어지면 Order가 생성되고 기본 상태는 PENDING이며 쿠폰은 적용되어있지 않다.")
	void shouldCreateOrderWithPendingStatusAndNoCoupon() {
		// given
		User user = Instancio.of(User.class)
			.set(org.instancio.Select.field("id"), 10L)
			.create();

		// when
		Order order = Order.create(user);

		assertAll(
			() -> assertThat(order).isNotNull(),
			() -> assertThat(order.getUserId()).isEqualTo(10L),
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING),
			() -> assertThat(order.getOrderProducts()).isEmpty(),
			() -> assertThat(order.getPublishedCouponId()).isNull(),
			() -> assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.ZERO),
			() -> assertThat(order.getDiscountedPrice()).isEqualTo(BigDecimal.ZERO)
		);
	}

	@Test
	@DisplayName("User가 null이면 IllegalArgumentException이 발생한다.")
	void createOrder_withNullUser_shouldThrow() {
		// when & then
		assertThatThrownBy(() -> Order.create(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId가 null입니다.");
	}

	@Test
	@DisplayName("정상적인 상품 2개 추가 시 OrderProduct가 추가되고 총합 가격이 계산된다.")
	void shouldAddOrderProductAndCalculateTotalPrice() {
		// given
		User user = Instancio.of(User.class)
			.set(org.instancio.Select.field("id"), 10L)
			.create();
		Order order = Order.create(user);
		Product product1 = Instancio.of(Product.class)
			.set(org.instancio.Select.field("id"), 1L)
			.set(org.instancio.Select.field("price"), BigDecimal.valueOf(1000L))
			.create();
		Product product2 = Instancio.of(Product.class)
			.set(org.instancio.Select.field("id"), 2L)
			.set(org.instancio.Select.field("price"), BigDecimal.valueOf(2000L))
			.create();

		Long quantity1 = 2L;
		Long quantity2 = 3L;

		BigDecimal expectedTotalPrice = product1.getPrice().multiply(BigDecimal.valueOf(quantity1))
			.add(product2.getPrice().multiply(BigDecimal.valueOf(quantity2)));

		// when
		order.addOrderProduct(product1, quantity1);
		order.addOrderProduct(product2, quantity2);

		assertAll(
			() -> assertThat(order.getOrderProducts()).hasSize(2),
			() -> assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice)
		);
	}

	@ParameterizedTest(name = "[{index}] quantity={0}")
	@NullSource
	@ValueSource(longs = {0L, -1L})
	@DisplayName("주문 상품 수량이 null 또는 0 이하이면 예외가 발생한다.")
	void addOrderProduct_invalidQuantity_shouldThrow(Long invalidQuantity) {
		// given
		Product product = Instancio.of(Product.class)
			.set(org.instancio.Select.field("price"), BigDecimal.valueOf(10000))
			.create();

		Order order = Order.create(Instancio.create(User.class));

		// when & then
		assertThatThrownBy(() -> order.addOrderProduct(product, invalidQuantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량이 null이거나 0 이하입니다.");
	}

	@Test
	@DisplayName("주문 상품이 null이면 예외가 발생한다.")
	void addOrderProduct_withNullProduct_shouldThrow() {
		// given
		Order order = Order.create(Instancio.create(User.class));

		// when & then
		assertThatThrownBy(() -> order.addOrderProduct(null, 1L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("상품이 null입니다.");
	}

	@Test
	@DisplayName("주문에 쿠폰을 적용하면 발급받은 쿠폰 아이디가 저장되고 쿠폰 적용 여부는 true다.")
	void shouldApplyCouponAndUpdateCouponStatus() {
		// given
		BigDecimal originalPrice = BigDecimal.valueOf(10000);
		BigDecimal discountAmount = BigDecimal.valueOf(5000);

		Order order = Instancio.of(Order.class)
			.set(Select.field("publishedCouponId"), null)
			.set(Select.field(Order.class, "totalPrice"), originalPrice)
			.set(Select.field(Order.class, "discountedPrice"), BigDecimal.ZERO)
			.create();

		Coupon coupon = Coupon.createUnlimited(
			"테스트 쿠폰",
			discountAmount,
			DiscountType.FIXED,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(7)
		);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(
			1L,
			coupon,
			LocalDate.now()
		);

		// when
		order.applyCoupon(publishedCoupon);

		// then
		assertAll(
			() -> assertThat(order.getDiscountedPrice()).isEqualByComparingTo(discountAmount),
			() -> assertThat(order.getPublishedCouponId()).isEqualTo(publishedCoupon.getId()),
			() -> assertThat(publishedCoupon.isUsed()).isTrue()
		);
	}

	@Test
	@DisplayName("주문 상태가 PENDING이면 paid() 호출 시 상태가 PAID로 변경된다.")
	void shouldChangeStatusToPaidWhenPending() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.create();

		// when
		order.paid();

		// then
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@ParameterizedTest
	@ValueSource(strings = {"PAID", "EXPIRED"})
	@DisplayName("주문 상태가 PENDING이 아니면 paid() 호출 시 OrderCannotBePaidException이 발생한다.")
	void shouldThrowExceptionWhenStatusIsNotPending(String status) {
		// given
		OrderStatus nonPendingStatus = OrderStatus.valueOf(status);

		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), nonPendingStatus)
			.create();

		// when & then
		assertThatThrownBy(order::paid)
			.isInstanceOf(OrderCannotBePaidException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("PENDING 상태의 주문만 결제할 수 있습니다.");
	}

	@Test
	@DisplayName("주문 상태가 PENDING이면 expire() 호출 시 상태가 EXPIRED로 변경되고 적용된 쿠폰을 null로 바꾼다.")
	void shouldChangeOrderStatusToExpired() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.create();

		// when
		order.expire();

		// then
		assertAll(
			() -> assertThat(order.getPublishedCouponId()).isNull(),
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED)
		);

	}

	@ParameterizedTest
	@ValueSource(strings = {"PAID", "EXPIRED"})
	@DisplayName("주문 상태가 PENDING이 아니면 expire() 호출 시 OrderCannotBeExpiredException이 발생한다.")
	void shouldThrowExceptionWhenExpireCalledOnNonPending(String status) {
		// given
		OrderStatus currentStatus = OrderStatus.valueOf(status);

		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "orderStatus"), currentStatus)
			.create();

		// when & then
		assertThatThrownBy(order::expire)
			.isInstanceOf(OrderCannotBeExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("결제 완료된 주문은 만료할 수 없습니다.");
	}

	@Test
	@DisplayName("이미 쿠폰이 적용된 주문에 다시 쿠폰을 적용하면 OrderAlreadyCouponAppliedException이 발생한다.")
	void shouldThrowExceptionWhenCouponIsAppliedTwice() {
		// given
		Order order = Instancio.of(Order.class)
			.set(Select.field(Order.class, "publishedCouponId"), 999L) // 이미 적용됨
			.set(Select.field(Order.class, "totalPrice"), BigDecimal.valueOf(10000))
			.set(Select.field(Order.class, "discountedPrice"), BigDecimal.ZERO)
			.create();

		Coupon coupon = Coupon.createUnlimited(
			"중복 쿠폰",
			BigDecimal.valueOf(1000),
			DiscountType.FIXED,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(7)
		);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(1L, coupon, LocalDate.now());

		// when & then
		assertThatThrownBy(() -> order.applyCoupon(publishedCoupon))
			.isInstanceOf(OrderAlreadyCouponAppliedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("이미 쿠폰이 적용된 주문입니다.");
	}
}
