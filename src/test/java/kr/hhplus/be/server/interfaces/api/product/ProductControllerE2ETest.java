package kr.hhplus.be.server.interfaces.api.product;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.infra.product.ProductJpaRepository;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductReadAllResponse;
import kr.hhplus.be.server.support.DbCleaner;
import kr.hhplus.be.server.support.E2ETestSupprot;

class ProductControllerE2ETest extends E2ETestSupprot {

	@LocalServerPort
	int port;

	@Autowired
	DbCleaner dbCleaner;

	@Autowired
	ProductJpaRepository productJpaRepository;

	@BeforeEach
	void setUp() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();

		dbCleaner.execute();
	}

	RestClient restClient;

	@Test
	@DisplayName("상품 목록 조회 API를 호출하면 상품 목록을 조회할 수 있다.")
	void getProducts() {
		// given
		// 상품 3개를 생성한다.
		List<Product> products = IntStream.rangeClosed(1, 5)
			.mapToObj(i -> Product.create("상품" + i, "설명" + i, BigDecimal.valueOf(1000L * i), 10L * i))
			.toList();
		productJpaRepository.saveAll(products);

		// when
		ApiResponse<ProductReadAllResponse> response = restClient.get()
			.uri("/api/v1/products")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.data().products()).hasSize(5);
	}

	@Test
	@DisplayName("상품 목록 조회 API를 호출하면 상품 목록을 조회할 수 있다. (상품 이름 검색)")
	void getProductsWithProductName() {
		// given
		// 상품 3개를 생성한다.
		List<Product> products = IntStream.rangeClosed(1, 5)
			.mapToObj(i -> Product.create("상품" + i, "설명" + i, BigDecimal.valueOf(1000L * i), 10L * i))
			.toList();
		productJpaRepository.saveAll(products);

		// when
		ApiResponse<ProductReadAllResponse> response = restClient.get()
			.uri(uriBuilder ->
				uriBuilder.path("/api/v1/products")
					.queryParam("productName", "상품1")
					.build()
			)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		assertThat(response.code()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.data().products()).hasSize(1);
	}

}