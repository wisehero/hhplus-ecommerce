package kr.hhplus.be.server.domain.coupon.issuePolicy;

import kr.hhplus.be.server.domain.coupon.Coupon;

public class UnlimitedIssuePolicy implements CouponIssuePolicy {
	@Override
	public boolean canIssue(Coupon coupon) {
		return true;
	}

	@Override
	public void issue(Coupon coupon) {
		// 무제한 쿠폰이라서 할일이 없다.
	}
}
