package kr.hhplus.be.server.domain.coupon.issuePolicy;

import kr.hhplus.be.server.domain.coupon.Coupon;

public interface CouponIssuePolicy {
	boolean canIssue(Coupon coupon); // 발급 가능 여부 판단

	void issue(Coupon coupon);
}
