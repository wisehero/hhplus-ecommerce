package kr.hhplus.be.server.application.order.dto;

import java.util.List;

import kr.hhplus.be.server.domain.coupon.dto.PublishedCouponFindCriteria;

public record OrderCreateCommand(
	Long userId,
	Long publishedCouponId,
	List<OrderLine> orderLines
) {

	public PublishedCouponFindCriteria toPublishedCouponFindCriteria() {
		return new PublishedCouponFindCriteria(userId, publishedCouponId);
	}
}
