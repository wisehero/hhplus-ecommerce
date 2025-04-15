package kr.hhplus.be.server.domain.product.exception;

import kr.hhplus.be.server.common.BusinessException;

public class ProductOutOfStockException extends BusinessException {

	private static final String DEFAULT_MESSAGE = "재고가 부족합니다. 현재 재고 : %s, 감소 시도 수량 : %s";

	public ProductOutOfStockException(Long currentStock, Long requestedQuantity) {
		super(DEFAULT_MESSAGE.formatted(currentStock, requestedQuantity));
	}
}
