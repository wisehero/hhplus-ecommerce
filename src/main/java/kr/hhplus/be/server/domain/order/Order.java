package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.order.exception.OrderAlreadyCouponAppliedException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBePaidException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseTimeEntity {

	@Id
	private Long id;

	private Long userId;

	private Long userCouponId;

	private BigDecimal totalPrice;

	private OrderStatus orderStatus;

	@Builder
	private Order(Long userId, Long userCouponId, BigDecimal totalPrice, OrderStatus orderStatus) {
		this.userId = userId;
		this.userCouponId = userCouponId;
		this.totalPrice = totalPrice;
		this.orderStatus = orderStatus;
	}

	public static Order create(Long userId, Long userCouponId, BigDecimal totalPrice) {
		return Order.builder()
			.userId(userId)
			.userCouponId(userCouponId)
			.totalPrice(totalPrice)
			.orderStatus(OrderStatus.PENDING)
			.build();
	}

	public void applyCoupon(Long userCouponId) {
		if (this.userCouponId != null) {
			throw new OrderAlreadyCouponAppliedException();
		}

		if (userCouponId == null) {
			throw new IllegalArgumentException("적용할 쿠폰 ID는 null일 수 없습니다.");
		}

		this.userCouponId = userCouponId;
	}

	public boolean hasCouponApplied() {
		return this.userCouponId != null;
	}

	public void paid() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new OrderCannotBePaidException();
		}
		this.orderStatus = OrderStatus.PAID;
	}

	public void expire() {
		if (this.orderStatus == OrderStatus.PAID) {
			throw new OrderCannotBeExpiredException();
		}
		this.orderStatus = OrderStatus.EXPIRED;
	}
}
