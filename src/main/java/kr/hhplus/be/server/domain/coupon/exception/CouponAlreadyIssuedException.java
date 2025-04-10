package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.common.BusinessException;

public class CouponAlreadyIssuedException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "이미 발급받은 쿠폰입니다.";

	public CouponAlreadyIssuedException() {
		super(DEFAULT_MESSAGE);
	}
}
