package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.order.exception.OrderAlreadyCouponAppliedException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;

class OrderTest {

	@Test
	@DisplayName("주문 초기 생성시 처음 상태는 PENDING이고 쿠폰이 적용되어있지 않은 상태다.")
	void shouldCreateOrderWithPendingStatusAndNoCoupon() {
		// given
		Long userId = 1L;

		// when
		Order order = Order.create(userId, null, BigDecimal.ZERO);

		// then
		assertAll(
			() -> assertThat(order.getUserId()).isEqualTo(userId),
			() -> assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.ZERO),
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING),
			() -> assertThat(order.getUserCouponId()).isNull()
		);
	}

	@Test
	@DisplayName("주문에 쿠폰을 적용하면 사용자 쿠폰 아이디가 저장된다.")
	void shouldApplyCouponAndUpdateCouponStatus() {
		// given
		Long userId = 1L;
		Long userCouponId = 10L;
		BigDecimal totalPrice = BigDecimal.valueOf(10000);
		Order order = Order.create(userId, null, totalPrice);

		// when
		order.applyCoupon(userCouponId);

		// then
		assertThat(order.getUserCouponId()).isEqualTo(userCouponId);
	}

	@Test
	@DisplayName("쿠폰이 적용된 주문이라면 쿠폰 적용 여부가 true이다.")
	void shouldReturnTrueIfCouponApplied() {
		// given
		Long userId = 1L;
		Long userCouponId = 10L;
		BigDecimal totalPrice = BigDecimal.valueOf(10000);
		Order order = Order.create(userId, null, totalPrice);
		order.applyCoupon(userCouponId);

		// when
		boolean isCouponApplied = order.hasCouponApplied();

		// then
		assertThat(isCouponApplied).isTrue();
	}

	@Test
	@DisplayName("쿠폰이 적용안된 주문이라면 쿠폰 적용 여부가 false이다.")
	void shouldReturnFalseIfCouponNotApplied() {
		// given
		Long userId = 1L;
		BigDecimal totalPrice = BigDecimal.valueOf(10000);
		Order order = Order.create(userId, null, totalPrice);

		// when
		boolean isCouponApplied = order.hasCouponApplied();

		// then
		assertThat(isCouponApplied).isFalse();
	}

	@Test
	@DisplayName("PENDING 상태의 주문은 PAID 상태로 변경할 수 있다.")
	void shouldChangeOrderStatusToPaid() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));

		// when
		order.paid();

		// then
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	@DisplayName("PENDING 상태의 주문은 EXPIRED 상태로 변경할 수 있다.")
	void shouldChangeOrderStatusToExpired() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));

		// when
		order.expire();

		// then
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
	}

	@DisplayName("쿠폰을 적용하지 않은 주문을 만료하면 쿠폰 ID는 null이고 상태는 EXPIRED이다.")
	@Test
	void shouldExpireOrderWithoutCouponAndKeepCouponFieldsNull() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));

		// when
		order.expire();

		// then
		assertAll(
			() -> assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED),
			() -> assertThat(order.getUserCouponId()).isNull()
		);
	}

	@Test
	@DisplayName("이미 쿠폰이 적용된 주문에 다시 쿠폰을 적용하면 OrderAlreadyCouponAppliedException이 발생한다.")
	void shouldThrowExceptionWhenCouponIsAppliedTwice() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));
		order.applyCoupon(10L);

		// expected
		assertThatThrownBy(
			() -> order.applyCoupon(20L))
			.isInstanceOf(OrderAlreadyCouponAppliedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("이미 쿠폰이 적용된 주문입니다.");
	}

	@Test
	@DisplayName("쿠폰 ID가 null일 경우에는 IllegalArgumentException이 발생한다.")
	void shouldThrowExceptionWhenCouponIdIsNull() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));

		// expected
		assertThatThrownBy(() ->
			order.applyCoupon(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("적용할 쿠폰 ID는 null일 수 없습니다.");
	}

	@Test
	@DisplayName("결제된 주문을 만료하려고 하면 OrderCannotBeExpiredException이 발생한다.")
	void shouldThrowExceptionWhenExpiringAlreadyPaidOrder() {
		// given
		Order order = Order.create(1L, null, BigDecimal.valueOf(10000));
		order.paid();

		// expected
		assertThatThrownBy(order::expire)
			.isInstanceOf(OrderCannotBeExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("결제 완료된 주문은 만료할 수 없습니다.");
	}
}