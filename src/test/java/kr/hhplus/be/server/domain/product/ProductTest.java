package kr.hhplus.be.server.domain.product;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;

class ProductTest {

	@Test
	@DisplayName("상품의 재고를 감소시킨다.")
	void reduceStockSuccess() {
		// given
		Product product = Product.builder()
			.productName("상품1")
			.price(BigDecimal.valueOf(10000))
			.stock(10L)
			.build();

		// when
		product.decreaseStock(1L);

		// then
		assertThat(product.getStock()).isEqualTo(9L);
	}

	@Test
	@DisplayName("상품의 재고를 증가시킨다.")
	void increaseStockSuccess() {
		// given
		Product product = Product.builder()
			.productName("상품1")
			.price(BigDecimal.valueOf(10000))
			.stock(10L)
			.build();

		// when
		product.increaseStock(1L);

		// then
		assertThat(product.getStock()).isEqualTo(11L);
	}

	@ParameterizedTest
	@MethodSource("provideValues")
	@DisplayName("상품의 재고를 증가시킬 때, 수량이 null이거나 0이하라면 예외가 발생한다.")
	void increaseStockFailIfQuantityIsNullOrLeoZero(Long invalidQuantity) {
		// given
		Product product = Product.builder()
			.productName("상품1")
			.price(BigDecimal.valueOf(10000))
			.stock(10L)
			.build();

		// expected
		assertThatThrownBy(() -> product.increaseStock(invalidQuantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량은 null이 아니며, 0보다 커야 합니다. 증가 시도 수량 : %d".formatted(invalidQuantity));
	}

	@ParameterizedTest
	@MethodSource("provideValues")
	@DisplayName("상품의 재고를 감소시킬 때, 수량이 null이거나 0이하라면 예외가 발생한다.")
	void reduceStockFailIfQuantityIsNullOrLoeZero(Long invalidQuantity) {
		// given
		Product product = Product.builder()
			.productName("상품1")
			.price(BigDecimal.valueOf(10000))
			.stock(10L)
			.build();

		// expected
		assertThatThrownBy(() -> product.decreaseStock(invalidQuantity))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("수량은 null이 아니며, 0보다 커야 합니다. 사용 시도 수량 : %d".formatted(invalidQuantity));
	}

	@Test
	@DisplayName("상품의 재고가 이미 0이라면 재고는 차감될 수 없다.")
	void reduceStockFailIfStockAlreadyZero() {
		// given
		Product product = Product.builder()
			.productName("상품1")
			.price(BigDecimal.valueOf(10000))
			.stock(0L)
			.build();

		// expected
		assertThatThrownBy(() -> product.decreaseStock(1L))
			.isInstanceOf(ProductOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("재고가 부족합니다. 현재 재고 : %d, 사용 시도 수량 : %d".formatted(0L, 1L));

	}

	private static Stream<Long> provideValues() {
		return Stream.of(0L, null, -1L);
	}

}