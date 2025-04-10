package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.common.BusinessException;

public class CouponDoesNotBelongToUserException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "사용자가 보유한 쿠폰이 아닙니다.";

	public CouponDoesNotBelongToUserException() {
		super(DEFAULT_MESSAGE);
	}
}
