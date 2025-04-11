package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.common.BusinessException;

public class CouponAlreadyUsedException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "쿠폰이 이미 사용되었습니다.";

	public CouponAlreadyUsedException() {
		super(DEFAULT_MESSAGE);
	}
}
