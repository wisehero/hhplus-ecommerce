package kr.hhplus.be.server.domain.bestseller.event.type;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BestSellerRealTimeUpdatedEvent extends ApplicationEvent {
	private final Long productId;
	private final String productName;
	private final Long qunatity;

	public BestSellerRealTimeUpdatedEvent(Object source, Long productId, String productName,
		Long qunatity) {
		super(source);
		this.productId = productId;
		this.productName = productName;
		this.qunatity = qunatity;
	}
}
