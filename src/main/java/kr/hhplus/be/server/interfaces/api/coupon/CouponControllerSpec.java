package kr.hhplus.be.server.interfaces.api.coupon;

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
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.interfaces.api.coupon.response.CouponReadAllResponse;

@Tag(
	name = "쿠폰 API",
	description = "쿠폰 API"
)
public interface CouponControllerSpec {

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
	void issueCoupon(@RequestBody CouponIssueRequest request);
}
