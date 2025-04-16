package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.common.BusinessException;

public class CouponDoesNotUsedException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "쿠폰이 사용되지 않았습니다.";

	public CouponDoesNotUsedException() {
		super(DEFAULT_MESSAGE);
	}
}
