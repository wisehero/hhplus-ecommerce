package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.common.BusinessException;

public class PointChargeFailedException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "포인트 충전이 실패했습니다. 다시 시도해주세요.";

	public PointChargeFailedException() {
		super(DEFAULT_MESSAGE);
	}
}
