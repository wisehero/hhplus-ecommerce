package kr.hhplus.be.server.interfaces.api.point;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderStatus;
import kr.hhplus.be.server.domain.point.Balance;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.infra.order.OrderJpaRepository;
import kr.hhplus.be.server.infra.order.OrderProductJpaRepository;
import kr.hhplus.be.server.infra.point.PointJpaRepository;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.point.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.request.PointUsageRequest;
import kr.hhplus.be.server.interfaces.api.point.response.PointOfUserReadResponse;
import kr.hhplus.be.server.support.DbCleaner;
import kr.hhplus.be.server.support.E2ETestSupprot;

class PointControllerE2ETest extends E2ETestSupprot {

	@LocalServerPort
	int port;

	@Autowired
	private PointJpaRepository pointJpaRepository;

	@Autowired
	private OrderJpaRepository orderJpaRepository;

	@Autowired
	private OrderProductJpaRepository orderProductJpaRepository;

	@Autowired
	DbCleaner dbCleaner;

	RestClient restClient;

	@BeforeEach
	void setUp() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();

		dbCleaner.execute();
	}

	@Test
	@DisplayName("포인트 조회에 성공하면 200 OK를 응답하고 userId와 잔액을 확인한다.")
	void getPointTest() {
		// given
		Point point = Instancio.of(Point.class)
			.ignore(field("id"))
			.set(field(Point.class, "userId"), 123L)
			.supply(field(Point.class, "balance"),
				() -> Balance.createBalance(BigDecimal.valueOf(1000)))
			.create();
		pointJpaRepository.save(point);

		// when
		ApiResponse<PointOfUserReadResponse> response = restClient.get()
			.uri("/api/v1/points?userId={userId}", point.getUserId())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.code()).isEqualTo(200),
			() -> assertThat(response.message()).isEqualTo("요청이 정상적으로 처리되었습니다."),
			() -> assertThat(response.data().userId()).isEqualTo(123L),
			() -> assertThat(response.data().balance()).isEqualByComparingTo("1000")
		);
	}

	@Test
	@DisplayName("포인트 충전 API 호출 시 성공하면 200 OK를 응답하고 UserId와 잔액을 반환한다.")
	void chargePointTest() {
		Long userId = 123L;
		Point point = Instancio.of(Point.class)
			.ignore(field("id"))
			.set(field(Point.class, "userId"), userId)
			.supply(field(Point.class, "balance"),
				() -> Balance.createBalance(BigDecimal.valueOf(1000)))
			.create();
		pointJpaRepository.save(point);

		PointChargeRequest request = new PointChargeRequest(userId, BigDecimal.valueOf(1000));

		// when
		ApiResponse<PointOfUserReadResponse> response = restClient.patch()
			.uri("/api/v1/points/charge")
			.body(request)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.code()).isEqualTo(200),
			() -> assertThat(response.message()).isEqualTo("요청이 정상적으로 처리되었습니다."),
			() -> assertThat(response.data().userId()).isEqualTo(123L),
			() -> assertThat(response.data().balance()).isEqualByComparingTo("2000")
		);
	}

	@ParameterizedTest
	@MethodSource("invalidChargeRequests")
	@DisplayName("포인트 충전시 사용자 ID가 null이거나, 잔액이 양의 정수가 아니면 400 에러를 리턴한다.")
	void invalidInputChargeTest(PointChargeRequest request,
		Map<String, String> expectedValidationErrors) {
		// when
		ErrorResponse errorResponse = restClient.patch()
			.uri("/api/v1/points/charge")
			.body(request)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
			})  // 예외 발생 방지
			.body(ErrorResponse.class);

		// then
		assertAll(
			() -> assertThat(errorResponse.code()).isEqualTo(400),
			() -> assertThat(errorResponse.message()).isEqualTo("유효성 검사 실패"),
			() -> assertThat(errorResponse.detail()).isEqualTo("한 개 이상의 필드가 유효하지 않습니다."),
			() -> assertThat(errorResponse.validationErrors()).containsAllEntriesOf(expectedValidationErrors)
		);
	}

	static Stream<Arguments> invalidChargeRequests() {
		return Stream.of(
			Arguments.of(
				new PointChargeRequest(null, BigDecimal.valueOf(1000)),
				Map.of("userId", "사용자 ID는 필수입니다.")
			),
			Arguments.of(
				new PointChargeRequest(123L, BigDecimal.ZERO),
				Map.of("chargeAmount", "충전할 포인트는 정수여야 합니다.")
			),
			Arguments.of(
				new PointChargeRequest(123L, BigDecimal.valueOf(-100)),
				Map.of("chargeAmount", "충전할 포인트는 정수여야 합니다.")
			),
			Arguments.of(
				new PointChargeRequest(null, BigDecimal.ZERO),
				Map.of(
					"userId", "사용자 ID는 필수입니다.",
					"chargeAmount", "충전할 포인트는 정수여야 합니다."
				)
			)
		);
	}

	@Test
	@DisplayName("포인트 사용 API 호출 시 성공하면 204 NO_CONTENT를 응답한다.")
	void usePointTest() {
		// given
		Long userId = 123L;
		Point point = Instancio.of(Point.class)
			.ignore(field("id"))
			.set(field(Point.class, "userId"), userId)
			.supply(field(Point.class, "balance"),
				() -> Balance.createBalance(BigDecimal.valueOf(1000)))
			.create();
		pointJpaRepository.save(point);

		Product product1 = Instancio.of(Product.class)
			.create();
		Product product2 = Instancio.of(Product.class)
			.create();

		Order order = Instancio.of(Order.class)
			.ignore(field("id"))
			.set(field(Order.class, "userId"), 123L)
			.set(field(Order.class, "orderStatus"), OrderStatus.PENDING)
			.set(field(Order.class, "totalPrice"), BigDecimal.valueOf(500))
			.set(field(Order.class, "discountedPrice"), BigDecimal.valueOf(400))
			.create();
		Order savedOrder = orderJpaRepository.save(order);
		OrderProduct orderProduct1 = OrderProduct.create(product1, 1L);
		orderProduct1.assignOrderId(savedOrder.getId());
		OrderProduct orderProduct2 = OrderProduct.create(product2, 1L);
		orderProduct2.assignOrderId(savedOrder.getId());
		orderProductJpaRepository.saveAll(List.of(orderProduct1, orderProduct2));

		PointUsageRequest request = new PointUsageRequest(savedOrder.getId(), userId);

		// when
		HttpStatusCode statusCode = restClient.patch()
			.uri("/api/v1/points/use")
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getStatusCode();

		// then
		assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@ParameterizedTest
	@MethodSource("invalidPointUsageRequests")
	@DisplayName("포인트 사용 실패 테스트: 필수 파라미터 검증 실패 시 ErrorResponse 반환")
	void pointUsage_fail_validation(
		PointUsageRequest request,
		Map<String, String> expectedValidationErrors
	) {
		// when
		ErrorResponse errorResponse = restClient.patch()
			.uri("/api/v1/points/use")
			.body(request)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
			})
			.body(ErrorResponse.class);

		// then
		assertAll(
			() -> assertThat(errorResponse.code()).isEqualTo(400),
			() -> assertThat(errorResponse.message()).isEqualTo("유효성 검사 실패"),
			() -> assertThat(errorResponse.detail()).isEqualTo("한 개 이상의 필드가 유효하지 않습니다."),
			() -> assertThat(errorResponse.validationErrors()).containsAllEntriesOf(expectedValidationErrors)
		);
	}

	static Stream<Arguments> invalidPointUsageRequests() {
		return Stream.of(
			Arguments.of(
				new PointUsageRequest(null, 1L),
				Map.of("orderId", "주문 ID는 필수입니다.")
			),
			Arguments.of(
				new PointUsageRequest(1L, null),
				Map.of("userId", "사용자 ID는 필수입니다.")
			),
			Arguments.of(
				new PointUsageRequest(null, null),
				Map.of(
					"orderId", "주문 ID는 필수입니다.",
					"userId", "사용자 ID는 필수입니다."
				)
			)
		);
	}
}
