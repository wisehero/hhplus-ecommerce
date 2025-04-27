package kr.hhplus.be.server.interfaces.api.coupon;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController implements CouponControllerSpec {

	private final CouponService couponService;

	@PostMapping("/issue")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void issueCoupon(@Valid @RequestBody CouponIssueRequest request) {
		couponService.issueCoupon(request.toIssueCommand());
	}
}
