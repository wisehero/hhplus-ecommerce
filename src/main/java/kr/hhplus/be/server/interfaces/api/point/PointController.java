package kr.hhplus.be.server.interfaces.api.point;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.point.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.request.PointUseRequest;
import kr.hhplus.be.server.interfaces.api.point.response.PointOfUserReadResponse;

@RestController
@RequestMapping("/api/v1/points")
public class PointController implements PointControllerSpec {

	@GetMapping
	public ApiResponse<PointOfUserReadResponse> getPointsOfUser(@RequestParam("userId") Long userId) {
		return ApiResponse.ok(new PointOfUserReadResponse(
			123L,
			10000L
		));
	}

	@PostMapping("/charge")
	public ResponseEntity<ApiResponse<PointOfUserReadResponse>> chargeUserPoints(
		@RequestBody PointChargeRequest request) {

		ApiResponse<PointOfUserReadResponse> response =
			ApiResponse.created(new PointOfUserReadResponse(123L, 10000L + request.chargeAmount()));
		return ApiResponse.toResponseEntity(response);
	}

	@PostMapping("/use")
	public ResponseEntity<Void> useUserPoints(@RequestBody PointUseRequest request) {

		return ResponseEntity.noContent().build();
	}
}
