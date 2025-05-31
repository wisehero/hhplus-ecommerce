package kr.hhplus.be.server.domain.coupon.event;

import kr.hhplus.be.server.domain.coupon.event.type.CouponIssueRequestEvent;

public interface CouponEventPublisher {

	void publish(CouponIssueRequestEvent event);
}
