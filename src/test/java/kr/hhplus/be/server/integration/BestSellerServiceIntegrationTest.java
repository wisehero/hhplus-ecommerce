package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Instancio.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerResult;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;
import kr.hhplus.be.server.infra.bestseller.BestSellerJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class BestSellerServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private BestSellerService bestSellerService;

	@Autowired
	private BestSellerJpaRepository bestSellerRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	@DisplayName("BestSeller 저장 시, ID가 자동 할당되고 동일한 데이터가 DB에 저장된다.")
	void testSaveBestSeller() {
		// given
		BestSeller bestSeller = of(BestSeller.class)
			.ignore(field(BestSeller.class, "id"))
			.create();

		// when
		BestSeller savedBestSeller = bestSellerService.save(bestSeller);

		// then
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId()).orElseThrow();

		assertAll(
			() -> assertThat(retrieved.getId()).isNotNull(),
			() -> assertThat(retrieved.getProductId()).isEqualTo(savedBestSeller.getProductId())
		);
	}

	@Test
	@DisplayName("BestSeller의 판매량 변경시 변경된 판매량이 조회된다.")
	void changeSalesCountTest() {
		// given: Instancio로 BestSeller 생성 (ID는 무시)
		BestSeller bestSeller = of(BestSeller.class)
			.ignore(field(BestSeller.class, "id"))
			.set(field(BestSeller.class, "salesCount"), 0L)
			.create();

		BestSeller savedBestSeller = bestSellerService.save(bestSeller);

		//when
		savedBestSeller.addSalesCount(3L);
		bestSellerService.save(bestSeller);

		// then: DB에서 다시 조회하여 저장된 데이터와 비교
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId()).orElseThrow();

		assertThat(retrieved.getSalesCount()).isEqualTo(3L);
	}

	@Test
	@DisplayName("사용자는 일간 인기 상품 100개를 판매량순에 따라서 조회할 수 있다.")
	void getBestSelllers() {
		// given
		IntStream.range(1, 110).forEach(i -> {
			BestSeller bestSeller = Instancio.of(BestSeller.class)
				.ignore(field(BestSeller.class, "id"))
				.set(field(BestSeller::getProductName), "테스트 상품 " + i)
				.set(field(BestSeller::getDescription), "테스트 상품 설명 " + i)
				.set(field(BestSeller::getPrice), BigDecimal.valueOf((Math.random() * 99000) + 1000))
				.set(field(BestSeller::getStock), 100L * i)
				.set(field(BestSeller::getSalesCount), 100L * (51 - i))
				.set(field(BestSeller::getUpdatedAt), LocalDateTime.now().minusDays(1))
				.set(field(BestSeller::getCreatedAt), LocalDateTime.now().minusDays(1))
				.create();

			bestSellerRepository.saveAndFlush(bestSeller);
		});

		// when
		BestSellerResult topBestSellersDaily = bestSellerService.getTopBestSellersDaily();

		// then
		List<BestSellerSimpleInfo> bestSellers = topBestSellersDaily.getBestSellers();
		assertAll(
			() -> assertThat(bestSellers).hasSize(100),
			() -> assertThat(bestSellers).isSortedAccordingTo(
				Comparator.comparing(BestSellerSimpleInfo::salesCount).reversed())
		);
	}

	@Test
	@DisplayName("실시간 인기 상품 10개의 상품 이름을 조회할 수 있다")
	void getTodayRealTimeRankingProductNamesWithLimit() {
		// given
		int limit = 10;
		String todayRankingKey =
			"bestseller:realtime:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String productNamePrefix = "bestseller:productname:" +
			LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		// Redis에 테스트 데이터 설정 (15개 상품)
		for (int i = 1; i <= 15; i++) {
			String productId = String.valueOf(i);
			String productName = "실시간 인기상품 " + i;

			// 내림차순 점수 부여 (15, 14, 13, ...)
			redisTemplate.opsForZSet().add(todayRankingKey, productId, 16 - i);
			redisTemplate.opsForHash().put(productNamePrefix, productId, productName);
		}

		// when
		List<String> topProducts = bestSellerService.getTodayRealTimeRankingProductNamesWithLimit(limit);

		// then
		assertAll(
			() -> assertThat(topProducts).isNotEmpty(),
			() -> assertThat(topProducts).hasSize(limit),
			() -> assertThat(topProducts).containsExactly(
				"실시간 인기상품 1",
				"실시간 인기상품 2",
				"실시간 인기상품 3",
				"실시간 인기상품 4",
				"실시간 인기상품 5",
				"실시간 인기상품 6",
				"실시간 인기상품 7",
				"실시간 인기상품 8",
				"실시간 인기상품 9",
				"실시간 인기상품 10"
			)
		);
	}
}

