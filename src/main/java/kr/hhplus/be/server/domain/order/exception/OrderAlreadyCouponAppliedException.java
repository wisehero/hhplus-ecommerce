package kr.hhplus.be.server.domain.order.exception;

import kr.hhplus.be.server.common.BusinessException;

public class OrderAlreadyCouponAppliedException extends BusinessException {

	private static final String DETAIL_MESSAGE = "이미 쿠폰이 적용된 주문입니다.";

	public OrderAlreadyCouponAppliedException() {
		super(DETAIL_MESSAGE);
	}
}
