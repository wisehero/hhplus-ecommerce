package kr.hhplus.be.server.interfaces.api.bestseller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.constants.BestSellerPeriod;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;

@Tag(
	name = "인기 상품 API",
	description = "인기 상품 관련 API입니다."
)
public interface BestSellerControllerSpec {

	@Operation(
		summary = "기간 설정 별 인기 상품 조회",
		description = "일간(DAILY), 주간(WEEKLY), 월간(MONTHLY) 중 하나를 선택해 인기 상품 목록을 조회합니다."
	)
	@ApiResponses(
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "인기 상품 조회 성공",
			content = @Content(
				schema = @Schema(implementation = BestSellerReadAllResponse.class),
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "인기 상품 조회 예시",
						value = """
							{
								"code": 200,
								"message": "요청이 정상적으로 처리되었습니다.",
								"data": {
									"products": [
										{
											"id": 1,
											"name": "상품1",
											"salesCount": 1000,
											"stock": 10000,
											"price": 100000
										},
										{
											"id": 2,
											"name": "상품2",
											"salesCount": 2000,
											"stock": 20000,
											"price": 20000
										}
									]
								}
							}
							"""
					)
				}
			)
		)
	)
	ApiResponse<BestSellerReadAllResponse> getBestSeller(
		@Parameter(
			description = "조회 기간 (DAILY, WEEKLY, MONTHLY)",
			example = "DAILY",
			required = true,
			schema = @Schema(implementation = BestSellerPeriod.class))
		@RequestParam BestSellerPeriod period);
}
