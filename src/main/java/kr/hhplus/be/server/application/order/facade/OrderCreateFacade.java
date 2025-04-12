package kr.hhplus.be.server.application.order.facade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.interfaces.api.order.request.OrderLine;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCreateFacade {

	private final OrderService orderService;
	private final ProductService productService;
	private final CouponService couponService;

	@Transactional
	public OrderCreateResult createOrder(OrderCreateCommand command) {

		List<OrderProduct> orderProducts = new ArrayList<>();
		BigDecimal totalPrice = BigDecimal.ZERO;

		// 상품 재고 감소 및 총 가격 계산
		for (OrderLine orderLine : command.orderLines()) {
			Product product = productService.decreaseStock(orderLine.productId(), orderLine.quantity());
			OrderProduct orderProduct = OrderProduct.create(product, orderLine.quantity());
			orderProducts.add(orderProduct);

			totalPrice = totalPrice.add(orderProduct.getAmount());
		}

		// 쿠폰 적용 여부 확인
		Long userCouponId = command.userCouponId();
		if (userCouponId != null) {
			UserCoupon userCoupon = couponService.getUserCouponById(command.userCouponId());
			totalPrice = couponService.applyCouponDiscount(
				command.userId(),
				userCoupon.getId(),
				totalPrice);
		}

		// 주문 생성
		Order orderWithoutCoupon = orderService.order(command.userId(), userCouponId, orderProducts, totalPrice);
		return OrderCreateResult.from(orderWithoutCoupon);
	}
}
