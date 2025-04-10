package kr.hhplus.be.server.domain.coupon.issuePolicy;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;

public class LimitedIssuePolicy implements CouponIssuePolicy {

	@Override
	public boolean canIssue(Coupon coupon) {
		return coupon.getRemainingCount() > 0;
	}

	@Override
	public void issue(Coupon coupon) {
		if (!canIssue(coupon)) {
			throw new CouponOutOfStockException(coupon.getId());
		}
		coupon.decreaseRemainingCount();
	}
}
