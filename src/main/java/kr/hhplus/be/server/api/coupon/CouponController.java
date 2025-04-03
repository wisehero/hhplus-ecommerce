package kr.hhplus.be.server.api.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.api.ApiResponse;
import kr.hhplus.be.server.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.api.coupon.response.CouponInfo;
import kr.hhplus.be.server.api.coupon.response.CouponReadAllResponse;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponControllerSpec {

	@GetMapping
	public ApiResponse<CouponReadAllResponse> getCoupons(@RequestParam("userId") Long userId) {
		return ApiResponse.ok(
			new CouponReadAllResponse(
				userId,
				List.of(
					new CouponInfo(
						1L,
						"10000원 할인 쿠폰",
						"AMOUNT",
						BigDecimal.valueOf(10000),
						LocalDate.of(2023, 10, 1),
						LocalDate.of(2023, 10, 31)
					),
					new CouponInfo(
						2L,
						"10000원 할인 쿠폰",
						"AMOUNT",
						BigDecimal.valueOf(10000),
						LocalDate.of(2023, 10, 1),
						LocalDate.of(2023, 10, 31)
					)
				)
			)
		);
	}

	@PostMapping("/issue")
	public ResponseEntity<Void> issueCoupon(@RequestBody CouponIssueRequest request) {
		return ResponseEntity.noContent().build();
	}
}
