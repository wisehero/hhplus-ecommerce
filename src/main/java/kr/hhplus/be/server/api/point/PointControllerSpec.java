package kr.hhplus.be.server.api.point;

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
import kr.hhplus.be.server.api.point.request.PointChargeRequest;
import kr.hhplus.be.server.api.point.request.PointUseRequest;
import kr.hhplus.be.server.api.point.response.PointOfUserReadResponse;

@Tag(
	name = "포인트 API",
	description = "포인트 관련 API입니다."
)
public interface PointControllerSpec {

	@Operation(summary = "사용자 포인트 조회", description = "사용자 ID를 입력받아 해당 사용자의 보유 포인트를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "포인트 조회 성공",
			content = @Content(
				schema = @Schema(implementation = PointOfUserReadResponse.class),
				mediaType = "application/json",
				examples = {
					@ExampleObject(name = "포인트 조회 성공 예시",
						value = """
							{
								"code": 200,
								"message": "요청이 정상적으로 처리되었습니다.",
								"data":{
									"userId": 1,
									"balance": 10000
								}
							}
							""")
				})),
	})
	ApiResponse<PointOfUserReadResponse> getPointsOfUser(
		@Parameter(
			description = "조회할 사용자 ID (1 이상의 정수)",
			schema = @Schema(type = "integer", minimum = "1", example = "123")
		)
		@RequestParam("userId") Long userId);

	@Operation(summary = "사용자 포인트 충전", description = "사용자 ID와 충전할 포인트를 입력받아 해당 사용자의 포인트를 충전합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "포인트 충전 성공",
			content = @Content(
				schema = @Schema(implementation = PointOfUserReadResponse.class),
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						value = """
							{
								"code": 201,
								"message": "요청이 정상적으로 처리되었습니다.",
								"data":{
									"userId": 1,
									"balance": 10000
								}
							}
							"""
					)
				}
			)
		),

		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "포인트 충전 실패 예시",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "포인트 충전 실패 : 1회 누적 금액을 초과해서 충전이 실패한 경우",
						value = """
							{
								"code": 409,
								"message": "비즈니스 정책을 위반한 요청입니다.",
								"detail": "포인트 잔액이 부족합니다. 현재 잔액 : 100,000원 결제 금액 : 200,000원"
							}
							"""
					),
					@ExampleObject(
						name = "포인트 충전 실패 : 주문 상태가 EXPIRED(결제 유효 기간 만료)이기 때문에 결제가 실패한 경우",
						value = """
							{
								"code": 409,
								"message": "비즈니스 정책을 위반한 요청입니다.",
								"detail": "주문 상태가 EXPIRED(결제 불가 건)입니다."
							}
							"""
					)
				}
			)
		)
	})
	ResponseEntity<ApiResponse<PointOfUserReadResponse>> chargeUserPoints(
		@Parameter(
			description = "사용자 포인트 충전 요청 DTO",
			schema = @Schema(implementation = PointChargeRequest.class))
		@RequestBody PointChargeRequest request);

	@Operation(
		summary = "사용자 포인트 사용",
		description = "주문 ID를 입력받아 해당 주문에 대해 사용자의 포인트를 사용합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "204",
			description = "포인트 사용 성공"
		),

		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "포인트 사용 실패 예시",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "포인트 사용 실패 : 결제 금액이 포인트보다 커서 결제가 실패한 경우",
						value = """
							{
								"code": 409,
								"message": "비즈니스 정책을 위반한 요청입니다.",
								"detail": "포인트 잔액이 부족합니다. 현재 잔액 : 100,000원 결제 금액 : 200,000원"
							}
							"""
					),
					@ExampleObject(
						name = "포인트 사용 실패 : 주문 상태가 EXPIRED(결제 유효 기간 만료)이�� 때문에 결제가 실패한 경우",
						value = """
							{
								"code": 409,
								"message": "비즈니스 정책을 위반한 요청입니다.",
								"detail": "주문 상태가 EXPIRED(결제 불가 건)입니다."
							}
							"""
					)
				}
			)
		)
	})
	ResponseEntity<Void> useUserPoints(
		@Parameter(
			description = "사용자 포인트 사용 요청 DTO",
			schema = @Schema(implementation = PointUseRequest.class))
		@RequestBody PointUseRequest request);
}
