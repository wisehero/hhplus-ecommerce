package kr.hhplus.be.server.domain.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {
	PENDING("결제 대기중"),
	PAID("결제 완료"),
	EXPIRED("주문 만료");

	private final String description;
}
