package kr.hhplus.be.server.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import kr.hhplus.be.server.domain.product.Product;

class OrderProductTest {


	@Test
	@DisplayName("정상적인 상품과 수량으로 OrderProduct를 생성할 수 있다")
	void createOrderProductSuccessfully() {
		Product product = mock(Product.class);
		when(product.getId()).thenReturn(1L);
		when(product.getPrice()).thenReturn(BigDecimal.valueOf(1000));

		OrderProduct orderProduct = OrderProduct.create(product, 3L);

		assertAll(
			() -> assertThat(orderProduct.getProductId()).isEqualTo(1L),
			() -> assertThat(orderProduct.getAmount()).isEqualTo(BigDecimal.valueOf(3000)),
			() -> assertThat(orderProduct.getQuantity()).isEqualTo(3L),
			() -> assertThat(orderProduct.getOrderId()).isNull()
		);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(longs = {0, -1})
	@DisplayName("수량이 null이거나 0 이하일 경우 IllegalArgumentException 예외가 발생한다")
	void createOrderProductFailsOnInvalidQuantity(Long quantity) {
		Product product = mock(Product.class);

		assertThatThrownBy(() -> OrderProduct.create(product, quantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("주문 수량은 0보다 커야 합니다.");
	}

	@Test
	@DisplayName("Order ID를 할당하면 orderId가 세팅된다")
	void assignOrderIdSetsOrderId() {
		Product product = mock(Product.class);
		when(product.getId()).thenReturn(1L);
		when(product.getPrice()).thenReturn(BigDecimal.valueOf(2000));

		OrderProduct orderProduct = OrderProduct.create(product, 2L);
		orderProduct.assignOrderId(99L);

		assertThat(orderProduct.getOrderId()).isEqualTo(99L);
	}
}