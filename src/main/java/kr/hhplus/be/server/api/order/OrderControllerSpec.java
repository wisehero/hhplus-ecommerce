package kr.hhplus.be.server.api.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.api.ApiResponse;
import kr.hhplus.be.server.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.response.OrderCreateResponse;

@Tag(
	name = "주문 API",
	description = "주문 관련 API입니다."
)
public interface OrderControllerSpec {

	@Operation(
		summary = "주문 생성",
		description = "사용자가 주문한 상품(들)이 담긴 주문을 생성합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "주문 생성 성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = OrderCreateResponse.class),
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "주문 생성 성공 예시",
						value = """
							{
								"code": 201,
								"message": "요청이 정상적으로 처리되었습니다.",
								"data": {
									"orderId": 1
								}
							}
							"""
					)
				}
			)
		),

		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "주문 생성 실패 예시",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "주문 생성 실패 : 쿠폰을 적용하였으나 보유한 쿠폰이 아니면 주문이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "사용자가 보유한 쿠폰이 아닙니다."
							}
							"""
					),
					@ExampleObject(
						name = "주문 생성 실패 : 쿠폰이 유효한 기간이 아니라서 주문이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "쿠폰이 유효한 기간이 아닙니다."
							}
							"""
					),
					@ExampleObject(
						name = "주문 생성 실패 : 이미 사용된 쿠폰을 적용하려고 해서 주문이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "이미 사용된 쿠폰입니다."
							}
							"""
					),
					@ExampleObject(
						name = "주문 생성 실패 : 재고가 부족해서 주문이 실패한 경우",
						value = """
							{
							  "code": 409,
							  "message": "비즈니스 정책을 위반한 요청입니다.",
							  "detail": "상품의 재고가 부족합니다."
							}
							"""
					)
				}
			)
		)}
	)
	ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
		@RequestBody OrderCreateRequest request);
}
