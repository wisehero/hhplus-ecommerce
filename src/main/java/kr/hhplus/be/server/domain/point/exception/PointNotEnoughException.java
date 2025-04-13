package kr.hhplus.be.server.domain.point.exception;

import java.math.BigDecimal;

import kr.hhplus.be.server.common.BusinessException;

public class PointNotEnoughException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "잔액이 부족합니다. 현재 포인트 : %s, 사용 시도 포인트 : %s";

	public PointNotEnoughException(BigDecimal currentPoint, BigDecimal requestedPoint) {
		super(DEFAULT_MESSAGE.formatted(currentPoint, requestedPoint));
	}
}
