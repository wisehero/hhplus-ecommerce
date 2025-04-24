package kr.hhplus.be.server.interfaces.api.order;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infra.product.ProductJpaRepository;
import kr.hhplus.be.server.infra.user.UserJpaRepository;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.interfaces.api.order.request.OrderProductRequest;
import kr.hhplus.be.server.interfaces.api.order.response.OrderCreateResponse;
import kr.hhplus.be.server.support.DbCleaner;
import kr.hhplus.be.server.support.E2ETestSupprot;

class OrderControllerE2ETest extends E2ETestSupprot {

	@LocalServerPort
	int port;

	@Autowired
	DbCleaner dbCleaner;

	@Autowired
	ProductJpaRepository productJpaRepository;

	@Autowired
	UserJpaRepository userJpaRepository;

	RestClient restClient;

	@BeforeEach
	void setUp() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();

		dbCleaner.execute();
	}

	@Test
	@DisplayName("주문 생성 API를 호출하면 주문을 생성하고 201 Created 응답을 반환한다.")
	void orderTest() {
		// given
		User user = Instancio.of(User.class)
			.ignore(field("id"))
			.create();
		User savedUser = userJpaRepository.save(user);

		Product product = Instancio.of(Product.class)
			.ignore(field("id"))
			.create();
		Product savedProduct = productJpaRepository.save(product);

		OrderProductRequest orderProductRequest = new OrderProductRequest(
			savedProduct.getId(),
			1L
		);
		OrderCreateRequest request = new OrderCreateRequest(
			savedUser.getId(),
			null,
			List.of(orderProductRequest)
		);

		// when
		ApiResponse<OrderCreateResponse> response = restClient.post()
			.uri("/api/v1/orders")
			.body(request)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.code()).isEqualTo(HttpStatus.CREATED.value()),
			() -> assertThat(response.data()).isNotNull(),
			() -> assertThat(response.data().orderId()).isNotNull()
		);
	}

	@ParameterizedTest
	@MethodSource("invalidRequests")
	@DisplayName("주문 생성 API를 호출하면 유효하지 않은 요청에 대해 400 Bad Request 응답을 반환한다.")
	void createOrderInvalidRequestTest(
		OrderCreateRequest invalidRequest,
		Map<String, String> expectedErrors
	) {
		// when
		ErrorResponse errorResponse = restClient.post()
			.uri("/api/v1/orders")
			.body(invalidRequest)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
			})
			.body(ErrorResponse.class);

		assertAll(
			() -> assertThat(errorResponse.code()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
			() -> assertThat(errorResponse.message()).isEqualTo("유효성 검사 실패"),
			() -> assertThat(errorResponse.detail()).isEqualTo("한 개 이상의 필드가 유효하지 않습니다."),
			() -> assertThat(errorResponse.validationErrors()).isEqualTo(expectedErrors)
		);
	}

	static Stream<Arguments> invalidRequests() {
		OrderProductRequest validProduct = new OrderProductRequest(1L, 1L);

		return Stream.of(
			Arguments.of(
				new OrderCreateRequest(null, 1L, List.of(validProduct)),
				Map.of("userId", "사용자 ID는 필수입니다.")
			),
			Arguments.of(
				new OrderCreateRequest(1L, 1L, null),
				Map.of("orderProducts", "주문할 상품 목록은 필수입니다.")
			),
			Arguments.of(
				new OrderCreateRequest(1L, 1L, List.of()),
				Map.of("orderProducts", "주문할 상품 목록은 필수입니다.")
			)
		);
	}
}