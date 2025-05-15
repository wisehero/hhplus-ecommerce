package kr.hhplus.be.server.interfaces.api.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.stream.Stream;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import kr.hhplus.be.server.infra.coupon.CouponJpaRepository;
import kr.hhplus.be.server.infra.coupon.PublishedCouponJpaRepository;
import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.support.E2ETestSupprot;

class CouponControllerE2ETest extends E2ETestSupprot {

	@Autowired
	CouponJpaRepository couponJpaRepository;

	@Autowired
	PublishedCouponJpaRepository publishedCouponJpaRepository;

	@Test
	@DisplayName("사용자 ID와 쿠폰 ID가 입력되면 쿠폰을 발급한다.")
	void couponIssueTest() {
		// given
		Long userId = 123L;
		Coupon coupon = Instancio.of(Coupon.class)
			.ignore(field("id"))   // ID는 DB에서 auto increment
			.set(field(Coupon.class, "remainingCount"), 10L)
			.set(field(Coupon.class, "issuePolicyType"), CouponIssuePolicyType.LIMITED)
			.create();
		Coupon savedCoupon = couponJpaRepository.save(coupon);

		CouponIssueRequest request = new CouponIssueRequest(userId, savedCoupon.getId());

		// when
		HttpStatusCode statusCode = restClient.post()
			.uri("/api/v1/coupons/issue")
			.body(request)
			.retrieve()
			.toBodilessEntity()
			.getStatusCode();

		// then
		PublishedCoupon publishedCoupon = publishedCouponJpaRepository.findByUserIdAndCouponId(userId,
			savedCoupon.getId()).orElseThrow();
		assertAll(
			() -> assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT),
			() -> assertThat(publishedCoupon.getCouponId()).isEqualTo(savedCoupon.getId()),
			() -> assertThat(publishedCoupon.getUserId()).isEqualTo(userId)
		);
	}

	@ParameterizedTest
	@MethodSource("invalidCouponIssueRequests")
	@DisplayName("쿠폰 발급 시 쿠폰 ID 혹은 사용자 ID가 실패라면 음수면 400 에러를 리턴한다.")
	void invalidInputCouponIssueTest(CouponIssueRequest request, Map<String, String> expectedValidationErrors) {
		// given

		// when
		ErrorResponse errorResponse = restClient.post()
			.uri("/api/v1/coupons/issue")
			.body(request)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
			})  // 예외 발생 방지
			.body(ErrorResponse.class);  // ErrorResponse로 바인딩

		assertAll(
			() -> assertThat(errorResponse.code()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
			() -> assertThat(errorResponse.message()).isEqualTo("유효성 검사 실패"),
			() -> assertThat(errorResponse.detail()).isEqualTo("한 개 이상의 필드가 유효하지 않습니다."),
			() -> assertThat(errorResponse.validationErrors()).containsAllEntriesOf(expectedValidationErrors)
		);
	}

	static Stream<Arguments> invalidCouponIssueRequests() {
		return Stream.of(
			Arguments.of(
				new CouponIssueRequest(null, 1L),
				Map.of("userId", "사용자 ID는 필수입니다.")
			),
			Arguments.of(
				new CouponIssueRequest(123L, null),
				Map.of("couponId", "쿠폰 ID는 필수입니다.")
			),
			Arguments.of(
				new CouponIssueRequest(null, null),
				Map.of(
					"userId", "사용자 ID는 필수입니다.",
					"couponId", "쿠폰 ID는 필수입니다."
				)
			)
		);
	}

}