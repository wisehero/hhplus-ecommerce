package kr.hhplus.be.server.domain.product;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	@DisplayName("모든 상품을 조회하는 테스트")
	void getAllProducts() {
		// given
		Product product1 = Product.create(
			"테스트 상품",
			"테스트 상품 설명",
			BigDecimal.valueOf(10000),
			10L
		);
		Product product2 = Product.create(
			"테스트 상품2",
			"테스트 상품 설명2",
			BigDecimal.valueOf(20000),
			20L
		);
		List<Product> expectedProducts = List.of(product1, product2);
		when(productRepository.findAll()).thenReturn(expectedProducts);

		// when
		List<Product> products = productService.getAllProducts();

		// then
		assertThat(products).hasSize(expectedProducts.size());
	}

	@Test
	@DisplayName("상품 재고를 차감하는 테스트")
	void descreaseStock() {
		// given
		Long productId = 1L;
		Long quantity = 5L;
		Product product = Product.create(
			"테스트 상품",
			"테스트 상품 설명",
			BigDecimal.valueOf(10000),
			10L);

		when(productRepository.findById(productId)).thenReturn(product);

		// when
		Product updatedProduct = productService.decreaseStock(productId, quantity);

		// then
		assertThat(updatedProduct.getStock()).isEqualTo(5L);
	}

	@Test
	@DisplayName("재고보다 더 많은 수량을 차감하려 하면 예외가 발생한다.")
	void decreaseStockFailInsufficientStock() {
		// given
		Long productId = 1L;
		Long quantity = 15L;
		Product product = Product.create(
			"테스트 상품",
			"테스트 상품 설명",
			BigDecimal.valueOf(10000),
			10L);

		when(productRepository.findById(productId)).thenReturn(product);

		// when, then
		assertThatThrownBy(() -> productService.decreaseStock(productId, quantity))
			.isInstanceOf(ProductOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("재고가 부족합니다. 현재 재고 : %s, 사용 시도 수량 : %s".formatted(product.getStock(), quantity));
	}

	@Test
	@DisplayName("상품의 재고를 성공적으로 복원(증가)할 수 있다")
	void restoreStock_success() {
		// given
		Long productId = 1L;
		Product product = Product.create("테스트 상품", "테스트 설명", BigDecimal.valueOf(1000), 5L);
		when(productRepository.findById(productId)).thenReturn(product);

		// when
		productService.restoreStock(productId, 3L);

		// then
		assertThat(product.getStock()).isEqualTo(8L);
	}
}