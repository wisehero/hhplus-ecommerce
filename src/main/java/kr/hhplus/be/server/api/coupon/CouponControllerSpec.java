package kr.hhplus.be.server.api.coupon;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.api.ApiResponse;
import kr.hhplus.be.server.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.response.CouponReadAllResponse;

@Tag(
	name = "쿠폰 API",
	description = "쿠폰 API"
)
public interface CouponControllerSpec {

	@Operation(
		summary = "쿠폰 조회",
		description = "사용자 ID를 입력받아 해당 사용자의 보유 쿠폰을 조회합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "쿠폰 조회 성공",
			content = @Content(
				schema = @Schema(implementation = CouponReadAllResponse.class),
				mediaType = "application/json",
				examples = {
					@ExampleObject
						(
							value = """
								{
								  "code": 200,
								  "message": "요청이 정상적으로 처리되었습니다.",
								  "data": {
								    "userId": 1,
								    "coupons": [
								      {
								        "couponId": 1,
								        "coupontTitle": "10% 할인 쿠폰",
								        "discountType": "RATE",
								        "discountValue": 10,
								        "startDate": "2025-08-01",
								        "endDate": "2025-08-31"
								      },
								      {
								        "couponId": 2,
								        "coupontTitle": "10,000원 할인 쿠폰",
								        "discountType": "AMOUNT",
								        "discountValue": 10000,
								        "startDate": "2025-08-01",
								        "endDate": "2025-08-31"
								      }
								    ]
								  }
								}
								"""
						)
				}
			)
		)
	})
	ApiResponse<CouponReadAllResponse> getCoupons(
		@Parameter(
			description = "조회할 사용자 ID (1 이상의 정수)",
			schema = @Schema(type = "integer", minimum = "1", example = "1")
		)
		@RequestParam("userId") Long userId);

	@Operation(
		summary = "선착순 쿠폰 발급",
		description = "사용자 ID와 쿠폰 ID를 입력받아 해당 사용자에게 쿠폰을 발급합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "204",
			description = "쿠폰 발급 성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "쿠폰 발급 실패 예시",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "쿠폰 발급 실패 : 쿠폰의 잔여 수량이 남지 않아 쿠폰 발급이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "쿠폰의 잔여 수량이 부족합니다."
							}
							"""
					),
					@ExampleObject(
						name = "쿠폰 발급 실패 : 이미 쿠폰을 발급 받아 쿠폰 발급이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "이미 쿠폰을 발급 받았습니다."
							}
							"""
					)
				}
			)
		)
	})
	ResponseEntity<Void> issueCoupon(@RequestBody CouponIssueRequest request);
}
