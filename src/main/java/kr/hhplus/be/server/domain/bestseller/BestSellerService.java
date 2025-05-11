package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerResult;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;
import kr.hhplus.be.server.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BestSellerService {

	private final BestSellerRepository bestSellerRepository;

	@Transactional
	public BestSeller save(BestSeller bestSeller) {
		return bestSellerRepository.save(bestSeller);
	}

	public BestSeller getProductInBestSeller(Product product) {
		return bestSellerRepository.findByProductId(product.getId())
			.orElseGet(() -> {
				// BestSeller 생성 (판매 수량 0으로 초기화)
				BestSeller newBestSeller = BestSeller.create(product, 0L);
				// 생성된 BestSeller를 DB에 저장 후 반환
				return bestSellerRepository.save(newBestSeller);
			});
	}

	// TODO : 추후 파라미터로 Period를 받아서 key를 결정
	// 일간 기준은 어제 00:00 ~ 어제 23:59
	@Cacheable(value = "bestSeller:daily", key = "'default'")
	public BestSellerResult getTopBestSellersDaily() {
		LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return fetchBestSellers(startOfYesterday, endOfYesterday);
	}

	// 주간 기준은 7일 전 00:00 ~ 어제 23:59
	@Cacheable(value = "bestSeller:weekly", key = "'default'")
	public BestSellerResult getTopBestSellersWeekly() {
		LocalDateTime startOfLastWeek = LocalDate.now().minusDays(7).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return fetchBestSellers(startOfLastWeek, endOfYesterday);
	}

	// 월간 기준은 30일 전 00:00 ~ 어제 23:59
	@Cacheable(value = "bestSeller:monthly", key = "'default'")
	public BestSellerResult getTopBestSellersMonthly() {
		LocalDateTime startOfLastMonth = LocalDate.now().minusDays(30).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return fetchBestSellers(startOfLastMonth, endOfYesterday);
	}

	// TODO : 추후 파라미터로 Period를 넣어서 비슷한 메소드의 중복 해결
	@CachePut(value = "bestSeller:daily", key = "'default'")
	public BestSellerResult refreshDailyCache() {
		LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return fetchBestSellers(startOfYesterday, endOfYesterday);
	}

	@CachePut(value = "bestSeller:weekly", key = "'default'")
	public List<BestSeller> refreshWeeklyCache() {
		LocalDateTime startOfLastWeek = LocalDate.now().minusDays(7).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return bestSellerRepository.findTop100DateBetween(startOfLastWeek, endOfYesterday);
	}

	@CachePut(value = "bestSeller:monthly", key = "'default'")
	public List<BestSeller> refreshMonthlyCache() {
		LocalDateTime startOfLastMonth = LocalDate.now().minusDays(30).atStartOfDay();
		LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay().minusSeconds(1);
		return bestSellerRepository.findTop100DateBetween(startOfLastMonth, endOfYesterday);
	}

	private BestSellerResult fetchBestSellers(LocalDateTime from, LocalDateTime to) {
		List<BestSellerSimpleInfo> findBestSellers = bestSellerRepository.findTop100DateBetween(from, to).stream()
			.map(BestSellerSimpleInfo::of)
			.toList();

		return new BestSellerResult(findBestSellers);
	}
}
