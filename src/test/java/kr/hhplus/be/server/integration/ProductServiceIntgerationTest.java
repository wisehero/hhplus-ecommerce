package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.support.IntgerationTestSupport;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.exception.ProductOutOfStockException;

public class ProductServiceIntgerationTest extends IntgerationTestSupport {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("상품 단건을 조회할 수 있다.")
	void shouldReturnProductWhenIdIsValid() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(Select.field(Product.class, "id")) // DB가 자동 생성할 ID는 무시
			.set(Select.field(Product.class, "productName"), "상품A")
			.set(Select.field(Product.class, "price"), BigDecimal.valueOf(10000))
			.set(Select.field(Product.class, "stock"), 10L)
			.create();

		productRepository.save(product); // 실제 DB 저장
		Long savedId = product.getId();

		// when
		Product result = productService.getProductById(savedId);

		// then
		assertAll(
			() -> assertThat(result.getId()).isEqualTo(savedId),
			() -> assertThat(result.getProductName()).isEqualTo("상품A"),
			() -> assertThat(result.getPrice()).isEqualByComparingTo("10000"),
			() -> assertThat(result.getStock()).isEqualTo(10L)
		);
	}

	@Test
	@DisplayName("상품 목록을 조회한다.")
	void test() {
		int numberOfProducts = 5;
		IntStream.rangeClosed(1, numberOfProducts)
			.forEach(i -> {
				Product product = Product.create(
					"상품" + i,
					"상품 설명" + i,
					BigDecimal.valueOf(i * 100L),
					1000L * i
				);
				productRepository.save(product);
			});

		// when
		List<Product> products = productService.getAllProducts();

		// then
		assertThat(products)
			.hasSize(numberOfProducts)
			.extracting(Product::getProductName)
			.containsExactlyInAnyOrder(
				IntStream.rangeClosed(1, numberOfProducts)
					.mapToObj(i -> "상품" + i)
					.toArray(String[]::new)
			);
	}

	// TODO 상품 재고 차감 테스트
	@Test
	@DisplayName("상품 재고는 주문 수량만큼 차감된다.")
	void shouldDecreaseStockWhenProductIsOrdered() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(Select.field(Product.class, "id"))
			.set(Select.field(Product.class, "stock"), 10L)
			.create();

		productRepository.save(product);
		Long savedId = product.getId();

		// when
		productService.decreaseStock(product, 3L);

		// then
		Product updated = productRepository.findById(savedId);

		assertThat(updated.getStock()).isEqualTo(7L);
	}

	@Test
	@DisplayName("상품 재고 차감이 실패하면 재고는 그대로다.")
	void shouldNotChangeStockWhenDecreaseFails() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(Select.field(Product.class, "id"))
			.set(Select.field(Product.class, "stock"), 5L)
			.create();

		productRepository.save(product);
		Long savedId = product.getId();

		// when & then
		assertThatThrownBy(() -> productService.decreaseStock(product, 10L)) // 재고 초과 차감
			.isInstanceOf(ProductOutOfStockException.class);

		Product updated = productRepository.findById(savedId);
		assertThat(updated.getStock()).isEqualTo(5L); // 재고는 그대로여야 한다
	}

	// TODO 상품 재고 원복(증가) 테스트
	@Test
	@DisplayName("상품 재고는 입력 수량만큼 정상적으로 증가한다.")
	void shouldIncreaseStockWhenValidQuantityIsGiven() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(Select.field(Product.class, "id"))
			.set(Select.field(Product.class, "stock"), 5L)
			.create();

		productRepository.save(product);
		Long savedId = product.getId();

		// when
		productService.restoreStock(savedId, 3L); // 5 + 3 = 8

		// then
		Product updated = productRepository.findById(savedId);
		assertThat(updated.getStock()).isEqualTo(8L);
	}

	@Test
	@DisplayName("재고 증가에 실패하면 상품 재고는 그대로다.")
	void shouldNotChangeStockWhenIncreaseFails() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(Select.field(Product.class, "id"))
			.set(Select.field(Product.class, "stock"), 5L)
			.create();

		productRepository.save(product);
		Long savedId = product.getId();

		// when & then
		assertThatThrownBy(() -> productService.restoreStock(savedId, 0L))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("수량이 null이거나 0 이하입니다.");

		// then
		Product updated = productRepository.findById(savedId);
		assertThat(updated.getStock()).isEqualTo(5L); // 재고 변화 없음
		}
}
