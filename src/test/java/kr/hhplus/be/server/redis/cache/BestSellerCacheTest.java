package kr.hhplus.be.server.redis.cache;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerResult;
import kr.hhplus.be.server.infra.bestseller.BestSellerJpaRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

// 일간, 주간, 월간 중 일간으로만 테스트
public class BestSellerCacheTest extends IntgerationTestSupport {

	@Autowired
	private BestSellerService bestSellerService;

	@Autowired
	private BestSellerJpaRepository bestSellerJpaRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@BeforeEach
	void setUpData() {
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

			bestSellerJpaRepository.saveAndFlush(bestSeller);
		});
	}

	@Test
	@DisplayName("일간 베스트셀러 캐시가 정상적으로 저장되는지 테스트")
	void dailyBestSellerCache() {
		// 캐시 초기 상태 확인
		Object cachedDataBefore = redisTemplate.opsForValue().get("bestSeller:daily::default");
		assertThat(cachedDataBefore).isNull(); // 캐시 비어있어야함

		// 캐시가 채워지도록 호출
		bestSellerService.getTopBestSellersDaily();

		// 캐시 확인
		Object cachedDataAfter = redisTemplate.opsForValue().get("bestSeller:daily::default");
		assertThat(cachedDataAfter).isNotNull(); // 캐시 비어있지 않아야함

		BestSellerResult actualResult = objectMapper.convertValue(cachedDataAfter, BestSellerResult.class);
		assertThat(actualResult.getBestSellers().size()).isEqualTo(100);
	}

	@Test
	@DisplayName("일간 베스트셀러가 정상적으로 갱신(@CachePut)되는지 테스트")
	void dailyBestSellerRefresh() {
		// 캐시 초기 상태 확인
		Object cachedDataBefore = redisTemplate.opsForValue().get("bestSeller:daily::default");
		assertThat(cachedDataBefore).isNull();

		// 캐시가 채워지도록 호출
		bestSellerService.refreshDailyCache();

		// 캐시 확인
		Object cachedDataAfter = redisTemplate.opsForValue().get("bestSeller:daily::default");
		assertThat(cachedDataAfter).isNotNull();

		// 캐시가 BestSellerResult 타입인지 확인
		BestSellerResult cachedResult = objectMapper.convertValue(cachedDataAfter, BestSellerResult.class);
		assertThat(cachedResult).isNotNull();
		assertThat(cachedResult.getBestSellers()).hasSize(100);
	}
}
