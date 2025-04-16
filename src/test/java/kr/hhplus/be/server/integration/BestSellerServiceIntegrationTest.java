package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Instancio.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerRepository;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class BestSellerServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private BestSellerService bestSellerService;

	@Autowired
	private BestSellerRepository bestSellerRepository;

	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("BestSeller 저장 시, ID가 자동 할당되고 동일한 데이터가 DB에 저장된다.")
	void testSaveBestSeller() {
		// given: Instancio로 BestSeller 생성 (ID는 무시)
		BestSeller bestSeller = of(BestSeller.class)
			.ignore(field(BestSeller.class, "id"))
			.create();

		// when: BestSeller 저장 (save 메서드가 호출됨)
		BestSeller savedBestSeller = bestSellerService.save(bestSeller);

		// then: DB에서 다시 조회하여 저장된 데이터와 비교
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId());

		assertAll(
			() -> assertThat(retrieved).isNotNull(),
			() -> assertThat(retrieved.getId()).isNotNull(),
			() -> assertThat(retrieved.getProductId()).isEqualTo(savedBestSeller.getProductId()),
			() -> assertThat(retrieved.getProductName()).isEqualTo(savedBestSeller.getProductName()),
			() -> assertThat(retrieved.getDescription()).isEqualTo(savedBestSeller.getDescription()),
			() -> assertThat(retrieved.getPrice()).isEqualByComparingTo(savedBestSeller.getPrice()),
			() -> assertThat(retrieved.getStock()).isEqualTo(savedBestSeller.getStock()),
			() -> assertThat(retrieved.getSalesCount()).isEqualTo(savedBestSeller.getSalesCount())
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
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId());

		assertThat(retrieved.getSalesCount()).isEqualTo(3L);
	}

	@Test
	@DisplayName("지정된 일 수 이내의 베스트셀러를 판매수 기준으로 상위 N개 가져온다")
	void shouldReturnTopBestSellersWithinDays() {
		// given
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 16, 0, 0, 0); // 또는 Clock 고정도 가능
		LocalDateTime threeDaysAgo = fixedNow.minusDays(3);

		List<BestSeller> expectedTop = IntStream.range(0, 5)
			.mapToObj(i -> Instancio.of(BestSeller.class)
				.ignore(field(BestSeller.class, "id"))
				.set(field(BaseTimeEntity.class, "createdAt"), threeDaysAgo.plusDays(i))
				.set(field(BestSeller.class, "salesCount"), 100L - (i * 10L)) // 100, 90, 80...
				.create())
			.toList();

		bestSellerRepository.saveAll(expectedTop);

		// when
		List<BestSeller> result = bestSellerService.getTopBestSellers(fixedNow, 3, 3); // 최근 3일간 상위 3개

		// then
		assertAll(
			() -> assertThat(result).hasSize(3),
			() -> assertThat(result)
				.isSortedAccordingTo(Comparator.comparingLong(BestSeller::getSalesCount).reversed()),
			() -> assertThat(result.get(0).getSalesCount()).isEqualTo(100L),
			() -> assertThat(result.get(1).getSalesCount()).isEqualTo(90L),
			() -> assertThat(result.get(2).getSalesCount()).isEqualTo(80L)
		);
	}

	@DisplayName("제품이 베스트셀러에 존재하면 해당 객체를 반환한다.")
	@Test
	void shouldReturnExistingOrCreateNewBestSeller() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();
		productRepository.save(product);

		// 이미 존재하는 경우
		BestSeller existing = BestSeller.create(product, 50L);
		bestSellerRepository.save(existing);

		// when
		BestSeller result = bestSellerService.getProductInBestSeller(product);

		// then
		assertAll(
			() -> assertThat(result.getId()).isEqualTo(existing.getId()),
			() -> assertThat(result.getProductId()).isEqualTo(product.getId()),
			() -> assertThat(result.getSalesCount()).isEqualTo(50L)
		);
	}

	@DisplayName("베스트셀러가 존재하지 않으면 새로 생성하여 반환한다")
	@Test
	void shouldCreateBestSellerIfNotExists() {
		// given
		Product product = Instancio.of(Product.class)
			.ignore(field(Product.class, "id"))
			.create();
		productRepository.save(product);

		// when
		BestSeller result = bestSellerService.getProductInBestSeller(product);

		// then
		assertAll(
			() -> assertThat(result.getProductId()).isEqualTo(product.getId()),
			() -> assertThat(result.getSalesCount()).isEqualTo(0L)
		);
	}
}
