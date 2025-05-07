package kr.hhplus.be.server.integration;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Instancio.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerRepository;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.support.IntgerationTestSupport;

public class BestSellerServiceIntegrationTest extends IntgerationTestSupport {

	@Autowired
	private BestSellerService bestSellerService;

	@Autowired
	private BestSellerRepository bestSellerRepository;

	@Test
	@DisplayName("BestSeller 저장 시, ID가 자동 할당되고 동일한 데이터가 DB에 저장된다.")
	void testSaveBestSeller() {
		// given
		BestSeller bestSeller = of(BestSeller.class)
			.ignore(field(BestSeller.class, "id"))
			.create();

		// when
		BestSeller savedBestSeller = bestSellerService.save(bestSeller);

		// then:
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId());

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
		BestSeller retrieved = bestSellerRepository.findById(savedBestSeller.getId());

		assertThat(retrieved.getSalesCount()).isEqualTo(3L);
	}
}
