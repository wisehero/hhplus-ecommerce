package kr.hhplus.be.server.interfaces.api.bestseller;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.infra.bestseller.BestSellerJpaRepository;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import kr.hhplus.be.server.support.E2ETestSupprot;

class BestSellerControllerE2ETest extends E2ETestSupprot {

	@Autowired
	BestSellerJpaRepository bestSellerJpaRepository;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Test
	@DisplayName("일간 Top 100 인기 상품을 상품을 조회한다.")
	void getBestSeller() {
		// given
		List<BestSeller> bestSellers = Instancio.ofList(BestSeller.class)
			.size(200)
			.supply(field(BestSeller.class, "id"), () -> null)
			.create();
		bestSellerJpaRepository.saveAll(bestSellers);

		// when
		ApiResponse<BestSellerReadAllResponse> response = restClient.get()
			.uri("/api/v1/bestsellers" + "?period=DAILY")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.code()).isEqualTo(200)
			// () -> assertThat(response.data().bestSellers().getBestSellers().size()).isLessThanOrEqualTo(100)
		);
	}

	@Test
	@DisplayName("실시간 인기 상품 랭킹을 조회한다.")
	void getTodayRealTimeRanking() {
		// given
		int limit = 10;
		String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String todayRankingKey = "bestseller:realtime:" + todayDate;
		String productNamePrefix = "bestseller:productname:" + todayDate;

		// Redis에 테스트 데이터 설정 (15개 상품)
		for (int i = 1; i <= 15; i++) {
			String productId = String.valueOf(i);
			String productName = "실시간 인기상품 " + i;

			redisTemplate.opsForZSet().add(todayRankingKey, productId, 16 - i);
			redisTemplate.opsForHash().put(productNamePrefix, productId, productName);
		}

		// when
		ApiResponse<List<String>> response = restClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/api/v1/bestsellers/realtime")
				.queryParam("limit", limit)
				.build())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response).isNotNull(),
			() -> assertThat(response.code()).isEqualTo(200),
			() -> assertThat(response.data()).isNotEmpty(),
			() -> assertThat(response.data()).hasSize(limit),
			() -> assertThat(response.data()).containsExactly(
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