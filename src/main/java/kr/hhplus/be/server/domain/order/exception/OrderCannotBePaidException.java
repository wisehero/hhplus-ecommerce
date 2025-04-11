package kr.hhplus.be.server.domain.order.exception;

import kr.hhplus.be.server.common.BusinessException;

public class OrderCannotBePaidException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "PENDING 상태의 주문만 결제할 수 있습니다.";

	public OrderCannotBePaidException() {
		super(DEFAULT_MESSAGE);
	}
}
