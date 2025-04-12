package kr.hhplus.be.server.domain.coupon.issuePolicy;

public enum CouponIssuePolicyType {
	LIMITED {
		@Override
		public CouponIssuePolicy toPolicy() {
			return new LimitedIssuePolicy();
		}
	},
	UNLIMITED {
		@Override
		public CouponIssuePolicy toPolicy() {
			return new UnlimitedIssuePolicy();
		}
	};

	public abstract CouponIssuePolicy toPolicy();
}
