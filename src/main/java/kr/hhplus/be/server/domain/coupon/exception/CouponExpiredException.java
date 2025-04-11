package kr.hhplus.be.server.domain.coupon.exception;

import java.time.LocalDate;

import kr.hhplus.be.server.common.BusinessException;

public class CouponExpiredException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.";

	public CouponExpiredException(LocalDate expireAt) {
		super(DEFAULT_MESSAGE.formatted( expireAt));
	}
}
