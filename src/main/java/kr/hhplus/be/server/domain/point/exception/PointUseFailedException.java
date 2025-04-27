package kr.hhplus.be.server.domain.point.exception;

import kr.hhplus.be.server.common.BusinessException;

public class PointUseFailedException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "포인트 사용에 실패했습니다. 다시 시도해주세요.";

	public PointUseFailedException() {
		super(DEFAULT_MESSAGE);
	}
}
