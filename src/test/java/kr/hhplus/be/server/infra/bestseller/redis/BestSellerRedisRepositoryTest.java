package kr.hhplus.be.server.infra.bestseller.redis;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerItem;
import kr.hhplus.be.server.support.RedisTestSupport;

class BestSellerRedisRepositoryTest extends RedisTestSupport {

	@Autowired
	private BestSellerRedisRepository bestSellerRedisRepository;

	private final String KEY_PREFIX_RANKING = "bestseller:realtime:" +
		LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	private final String KEY_PREFIX_PRODUCT_NAME = "bestseller:productname:" +
		LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

	@Test
	@DisplayName("상품 판매정보 추가 시 레디스에 점수가 저장된다")
	void incrementScore_shouldStoreInRedis() {
		// given
		BestSellerItem item = new BestSellerItem(1L, "테스트 상품", 3L);

		// when
		bestSellerRedisRepository.incrementScore(item);

		// then
		Double score = redisTemplate.opsForZSet().score(KEY_PREFIX_RANKING, "1");
		Object productName = redisTemplate.opsForHash().get(KEY_PREFIX_PRODUCT_NAME, "1");

		assertAll(
			() -> assertThat(score).isEqualTo(3.0),
			() -> assertThat(productName).isEqualTo("테스트 상품")
		);
	}

	@Test
	@DisplayName("상품 순위와 상품 순위에 속한 상품 이름은 저장 후 이틀 후에 만료되도록 TTL이 설정된다")
	void incrementScore_shouldSetTwoDaysTTL() {
		// given
		BestSellerItem item = new BestSellerItem(1L, "테스트 상품", 3L);

		// when
		bestSellerRedisRepository.incrementScore(item);

		// then
		// 랭킹 키와 상품명 키 모두 TTL이 설정되어 있는지 확인
		Long rankingTTL = redisTemplate.getExpire(KEY_PREFIX_RANKING);
		Long productNameTTL = redisTemplate.getExpire(KEY_PREFIX_PRODUCT_NAME);

		// TTL이 설정되어 있는지 확인 (양수면 TTL이 설정됨)
		assertThat(rankingTTL).isPositive();
		assertThat(productNameTTL).isPositive();

		// TTL이 대략 2일 (172800초) 정도인지 확인
		// 정확한 초 단위까지 맞출 필요는 없으므로 범위로 체크
		long twoDaysInSeconds = 2 * 24 * 60 * 60;
		assertThat(rankingTTL).isLessThanOrEqualTo(twoDaysInSeconds);
		assertThat(rankingTTL).isGreaterThan(twoDaysInSeconds - 300); // 5분 정도 여유

		assertThat(productNameTTL).isLessThanOrEqualTo(twoDaysInSeconds);
		assertThat(productNameTTL).isGreaterThan(twoDaysInSeconds - 300);
	}

	@Test
	@DisplayName("동일 상품 판매정보가 여러번 추가되면 점수가 누적된다")
	void incrementScore_shouldAccumulateScores() {
		// given
		BestSellerItem item1 = new BestSellerItem(1L, "테스트 상품", 3L);
		BestSellerItem item2 = new BestSellerItem(1L, "테스트 상품", 2L);

		// when
		bestSellerRedisRepository.incrementScore(item1);
		bestSellerRedisRepository.incrementScore(item2);

		// then
		Double score = redisTemplate.opsForZSet().score(KEY_PREFIX_RANKING, "1");
		assertThat(score).isEqualTo(5.0);
	}

	@Test
	@DisplayName("오늘의 베스트셀러 상위 10개를 조회한다")
	void getTodayTopProductNames_shouldReturnTopN() {
		// given
		for (long i = 1; i <= 10; i++) {
			long score = 11 - i;  // 1부터 10까지 역순으로 점수 부여 (10, 9, 8, ..., 1)
			bestSellerRedisRepository.incrementScore(new BestSellerItem(i, "상품" + i, score));
		}

		// when
		List<String> topProducts = bestSellerRedisRepository.getTodayTopProductNames(10);

		// then
		assertAll(
			() -> assertThat(topProducts).isNotEmpty(),
			() -> assertThat(topProducts).hasSize(10),
			() -> assertThat(topProducts).containsExactly(
				"상품1", "상품2", "상품3", "상품4", "상품5",
				"상품6", "상품7", "상품8", "상품9", "상품10"
			)
		);
	}

	@Test
	@DisplayName("오늘 날짜의 실시간 인기상품 데이터가 10개 미만이면 어제 데이터를 반환한다.")
	void shouldReturnYesterdayDataIfTodayDataIsInsufficient() {
		// given
		String yesterdayDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String yesterdayRankingKey = "bestseller:realtime:" + yesterdayDate;
		String yesterdayProductNameKey = "bestseller:productname:" + yesterdayDate;

		for (long i = 1; i <= 15; i++) {
			String productId = String.valueOf(i);
			redisTemplate.opsForZSet().add(yesterdayRankingKey, productId, 16 - i); // 15, 14, 13 ... 점수 부여
			redisTemplate.opsForHash().put(yesterdayProductNameKey, productId, "어제상품" + i);
		}

		for (long i = 101; i <= 105; i++) {
			bestSellerRedisRepository.incrementScore(new BestSellerItem(i, "오늘상품" + i, 20 - i)); // 오늘 상품은 5개
		}

		// when
		List<String> topProductNames = bestSellerRedisRepository.getTodayTopProductNames(10);

		// then
		assertAll(
			() -> assertThat(topProductNames).isNotEmpty(),
			() -> assertThat(topProductNames).hasSize(10),
			() -> assertThat(topProductNames).containsExactly(
				"어제상품1", "어제상품2", "어제상품3", "어제상품4", "어제상품5",
				"어제상품6", "어제상품7", "어제상품8", "어제상품9", "어제상품10"
			)
		);
	}

	@Test
	@DisplayName("오늘 데이터가 10개 이상이면 오늘 데이터를 반환한다")
	void getTodayTopProductNames_shouldReturnTodayDataIfSufficient() {
		// given
		// 어제 데이터 직접 삽입
		String yesterdayDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String yesterdayRankingKey = "bestseller:realtime:" + yesterdayDate;
		String yesterdayProductNameKey = "bestseller:productname:" + yesterdayDate;

		for (long i = 1; i <= 15; i++) {
			String productId = String.valueOf(i);
			redisTemplate.opsForZSet().add(yesterdayRankingKey, productId, 16 - i);
			redisTemplate.opsForHash().put(yesterdayProductNameKey, productId, "어제상품" + i);
		}

		// 오늘 데이터는 10개 이상 (충분함)
		for (long i = 101; i <= 112; i++) {
			bestSellerRedisRepository.incrementScore(new BestSellerItem(i, "오늘상품" + i, 120 - i));
		}

		// when
		List<String> topProducts = bestSellerRedisRepository.getTodayTopProductNames(10);

		// then
		assertAll(
			() -> assertThat(topProducts).isNotEmpty(),
			() -> assertThat(topProducts).hasSize(10),
			// 오늘 데이터의 점수 순서대로 상위 10개 확인
			() -> assertThat(topProducts).containsExactly(
				"오늘상품101", "오늘상품102", "오늘상품103", "오늘상품104", "오늘상품105",
				"오늘상품106", "오늘상품107", "오늘상품108", "오늘상품109", "오늘상품110"
			)
		);
	}
}