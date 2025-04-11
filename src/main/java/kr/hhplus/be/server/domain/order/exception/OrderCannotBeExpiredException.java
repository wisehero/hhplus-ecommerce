package kr.hhplus.be.server.domain.order.exception;

import kr.hhplus.be.server.common.BusinessException;

public class OrderCannotBeExpiredException extends BusinessException {

	private static final String DETAIL_MESSAGE = "결제 완료된 주문은 만료할 수 없습니다.";

	public OrderCannotBeExpiredException() {
		super(DETAIL_MESSAGE);
	}
}
