package kr.hhplus.be.server.application.order.facade;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.hhplus.be.server.application.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
import kr.hhplus.be.server.application.order.dto.OrderProductInfo;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;

@ExtendWith(MockitoExtension.class)
class OrderCreateFacadeTest {

	@Mock
	private ProductService productService;
	@Mock
	private CouponService couponService;
	@Mock
	private OrderService orderService;

	@InjectMocks
	private OrderCreateFacade orderCreateFacade;

	@Test
	@DisplayName("쿠폰 없이 주문을 생성하면 재고 차감 후 할인 가격이 적용 안된 주문이 생성된다")
	void createOrderWithoutCoupon() {
		// given
		Long userId = 1L;
		Long productId = 10L;
		Long quantity = 2L;
		BigDecimal price = BigDecimal.valueOf(1000);

		Product product = Product.create("상품명", "설명", price, 100L);
		OrderProductInfo orderProductInfo = OrderProductInfo.of(productId, quantity);
		OrderCreateCommand command = new OrderCreateCommand(userId, null, List.of(orderProductInfo));

		when(productService.decreaseStock(productId, quantity)).thenReturn(product);

		BigDecimal expectedTotal = price.multiply(BigDecimal.valueOf(quantity));
		Order order = Order.create(userId, null, price.multiply(BigDecimal.valueOf(quantity)));
		when(orderService.order(eq(userId), isNull(), anyList(), eq(expectedTotal))).thenReturn(order);

		// when
		OrderCreateResult result = orderCreateFacade.createOrder(command);

		// then
		assertAll(
			() -> assertThat(result).isNotNull(),
			() -> verify(productService, times(1)).decreaseStock(productId, quantity),
			() -> verify(couponService, never()).applyCouponDiscount(any(), any(), any()),
			() -> verify(orderService, times(1)).order(eq(userId), isNull(), anyList(), eq(expectedTotal))
		);
	}

	@Test
	@DisplayName("쿠폰을 사용한 주문은 할인 적용 후 주문이 생성된다")
	void createOrderWithCoupon() {
		// given
		Long userId = 1L;
		Long productId = 10L;
		Long quantity = 2L;
		Long userCouponId = 999L;

		BigDecimal price = BigDecimal.valueOf(1000); // 상품 가격
		BigDecimal originalTotal = price.multiply(BigDecimal.valueOf(quantity)); // 2000
		BigDecimal discountedTotal = BigDecimal.valueOf(1500); // 할인 후 금액

		Product product = Product.create("라떼", "달콤한 커피", price, 100L);
		ReflectionTestUtils.setField(product, "id", productId);

		OrderProductInfo orderProductInfo = new OrderProductInfo(productId, quantity);
		OrderCreateCommand command = new OrderCreateCommand(userId, userCouponId, List.of(orderProductInfo));

		PublishedCoupon userCoupon = PublishedCoupon.createUserCoupon(userId, 123L, "10% 쿠폰", LocalDate.now(),
			LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(userCoupon, "id", userCouponId);

		// stub
		when(productService.decreaseStock(eq(productId), eq(quantity))).thenReturn(product);
		when(couponService.getUserCouponById(userCouponId)).thenReturn(userCoupon);
		when(couponService.applyCouponDiscount(userId, userCouponId, originalTotal)).thenReturn(discountedTotal);

		Order discountedOrder = Order.create(userId, userCouponId, discountedTotal);
		when(orderService.order(eq(userId), eq(userCouponId), anyList(), eq(discountedTotal))).thenReturn(
			discountedOrder);

		// when
		OrderCreateResult result = orderCreateFacade.createOrder(command);

		// then
		assertAll(
			() -> assertThat(result).isNotNull(),
			() -> verify(productService).decreaseStock(productId, quantity),
			() -> verify(couponService).getUserCouponById(userCouponId),
			() -> verify(couponService).applyCouponDiscount(userId, userCouponId, originalTotal),
			() -> verify(orderService).order(eq(userId), eq(userCouponId), anyList(), eq(discountedTotal))
		);
	}

}