package kr.hhplus.be.server.domain.coupon.exception;

import kr.hhplus.be.server.common.BusinessException;

public class CouponOutOfStockException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "쿠폰 ID : %d는 모두 소진되었습니다.";

	public CouponOutOfStockException(Long couponId) {
		super(DEFAULT_MESSAGE.formatted(couponId));
	}
}
