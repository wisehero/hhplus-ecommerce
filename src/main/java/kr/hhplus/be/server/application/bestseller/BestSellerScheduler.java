package kr.hhplus.be.server.application.bestseller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BestSellerScheduler {

	private final OrderService orderService;
	private final ProductService productService;
	private final BestSellerService bestSellerService;

	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void insertBestSeller() {
		LocalDateTime now = LocalDateTime.now();

		// 1시간 전부터 현재까지의 주문을 가져옴
		List<Order> findOrders = orderService.getPaidOrdersWithinOneHour(now);

		Map<Long, Long> productSalesMap = findOrders.stream()
			.flatMap(order -> order.getOrderProducts().stream())
			.collect(Collectors.groupingBy(
				OrderProduct::getProductId,
				Collectors.summingLong(OrderProduct::getQuantity)
			));

		productSalesMap.forEach((productId, salesQuantity) -> {
				// Product 정보를 조회합니다.
				Product product = productService.getProductById(productId);
				if (product == null) {
					throw new IllegalArgumentException("Product not found for ID: " + productId);
				}
				// BestSeller 객체를 생성합니다.
				BestSeller productInBestSeller = bestSellerService.getProductInBestSeller(product);
				productInBestSeller.addSalesCount(salesQuantity);
				bestSellerService.save(productInBestSeller); // TODO 단건씩 업데이트 쳐야할까?
			}
		);
	}
}
