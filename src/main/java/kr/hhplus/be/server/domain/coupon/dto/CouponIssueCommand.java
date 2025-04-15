package kr.hhplus.be.server.domain.coupon.dto;

public record CouponIssueCommand(
	Long couponId,
	Long userId
) {
}
