package kr.hhplus.be.server.domain.coupon.dto;

public record CouponIssueCommand(
	Long userId,
	Long couponId
) {
}
