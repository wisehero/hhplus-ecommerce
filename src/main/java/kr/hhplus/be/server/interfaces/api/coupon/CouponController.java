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
		couponService.issueCouponV2(request.toIssueCommand());
	}

	@PostMapping("/issue/spin")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void issueCouponV3(@Valid @RequestBody CouponIssueRequest request) {
		couponService.issueCouponV3(request.toIssueCommand());
	}

	@PostMapping("/issue/pubsub")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void issueCouponV2(@Valid @RequestBody CouponIssueRequest request) {
		couponService.issueCouponV4(request.toIssueCommand());
	}

	@PostMapping("/issue/redis")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void issueCouponV4(@Valid @RequestBody CouponIssueRequest request) {
		couponService.issueWithRedis(request.toIssueCommand());
	}

	@PostMapping("/issue/kafka")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void issueCouponV5(@Valid @RequestBody CouponIssueRequest request){
		couponService.issueRequest(request.toIssueCommand());
	}
}
