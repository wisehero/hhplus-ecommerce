package kr.hhplus.be.server.domain.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.order.exception.OrderAlreadyCouponAppliedException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBeExpiredException;
import kr.hhplus.be.server.domain.order.exception.OrderCannotBePaidException;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Order extends BaseTimeEntity {

	private Long id;
	private Long userId;
	private Long publishedCouponId;
	private List<OrderProduct> orderProducts;
	private OrderStatus orderStatus;
	private BigDecimal totalPrice;
	private BigDecimal discountedPrice;

	private Order(Long userId, Long publishedCouponId, List<OrderProduct> orderProducts, OrderStatus orderStatus,
		BigDecimal totalPrice, BigDecimal discountedPrice) {
		this.userId = userId;
		this.publishedCouponId = publishedCouponId;
		this.orderProducts = orderProducts;
		this.orderStatus = orderStatus;
		this.totalPrice = totalPrice;
		this.discountedPrice = discountedPrice;
	}

	public static Order create(User user) {
		if (user == null)
			throw new IllegalArgumentException("userId가 null입니다.");
		return new Order(
			user.getId(),
			null,
			new ArrayList<>(),
			OrderStatus.PENDING,
			BigDecimal.ZERO,
			BigDecimal.ZERO);
	}

	public void addOrderProduct(Product product, Long quantity) {
		if (quantity == null || quantity <= 0) {
			throw new IllegalArgumentException("수량이 null이거나 0 이하입니다.");
		}

		if (product == null) {
			throw new IllegalArgumentException("상품이 null입니다.");
		}
		OrderProduct orderProduct = OrderProduct.create(product, quantity);
		this.orderProducts.add(orderProduct);
		this.totalPrice = this.totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
	}

	public void applyCoupon(PublishedCoupon publishedCoupon) {
		if (this.publishedCouponId != null) {
			throw new OrderAlreadyCouponAppliedException();
		}
		BigDecimal discountedPrice = publishedCoupon.discount(this.totalPrice, LocalDate.now());
		this.discountedPrice = this.discountedPrice.add(discountedPrice);
		this.publishedCouponId = publishedCoupon.getId();
	}

	public void paid() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new OrderCannotBePaidException();
		}
		this.orderStatus = OrderStatus.PAID;
	}

	public void expire() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new OrderCannotBeExpiredException();
		}
		this.publishedCouponId = null;
		this.orderStatus = OrderStatus.EXPIRED;
	}

	public boolean isCouponApplied() {
		return this.publishedCouponId != null;
	}
}
