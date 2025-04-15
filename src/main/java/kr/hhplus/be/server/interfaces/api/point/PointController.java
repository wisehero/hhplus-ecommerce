package kr.hhplus.be.server.interfaces.api.point;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.point.PointOrderPaymentFacade;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.point.dto.PointChargeCommand;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.point.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.request.PointUsageRequest;
import kr.hhplus.be.server.interfaces.api.point.response.PointOfUserReadResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController implements PointControllerSpec {

	private final PointService pointService;
	private final PointOrderPaymentFacade pointOrderPaymentFacade;

	@GetMapping
	public ApiResponse<PointOfUserReadResponse> getPointOfUser(@RequestParam("userId") Long userId) {
		Point userPoint = pointService.getPointOfUser(userId);
		return ApiResponse.ok(new PointOfUserReadResponse(userPoint));
	}

	@PatchMapping("/charge")
	public ApiResponse<PointOfUserReadResponse> chargeUserPoint(@RequestBody PointChargeRequest request) {
		PointChargeCommand command = request.toCommand();
		Point userPoint = pointService.chargeUserPoint(command);
		return ApiResponse.ok(new PointOfUserReadResponse(userPoint));
	}

	@PatchMapping("/use")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void useUserPoint(@RequestBody PointUsageRequest request) {
		pointOrderPaymentFacade.pointPayment(request.toCommand());
	}
}
