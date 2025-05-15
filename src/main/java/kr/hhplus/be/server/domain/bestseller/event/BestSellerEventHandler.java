package kr.hhplus.be.server.domain.bestseller.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import kr.hhplus.be.server.domain.bestseller.BestSellerRepository;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerItem;
import kr.hhplus.be.server.domain.bestseller.event.type.BestSellerRealTimeUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BestSellerEventHandler {

	private final BestSellerRepository bestSellerRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBestSellerUpdatedEvent(BestSellerRealTimeUpdatedEvent event) {
		Long productId = event.getProductId();
		String productName = event.getProductName();
		Long quantity = event.getQunatity();

		log.info("[이벤트 핸들러] 베스트셀러 업데이트 이벤트 처리 시작 Product ID : {}, Product Name: {}, Quantity: {}",
			productId, productName, quantity);
		bestSellerRepository.incrementScore(new BestSellerItem(
			productId, productName, quantity
		));
	}
}
