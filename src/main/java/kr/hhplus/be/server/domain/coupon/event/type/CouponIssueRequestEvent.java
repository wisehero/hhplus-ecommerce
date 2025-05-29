package kr.hhplus.be.server.domain.coupon.event.type;

public record CouponIssueRequestEvent(
	Long userId,
	Long couponId
) {
}
