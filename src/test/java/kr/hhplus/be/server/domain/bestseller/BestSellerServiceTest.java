package kr.hhplus.be.server.domain.bestseller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;

@ExtendWith(MockitoExtension.class)
class BestSellerServiceTest {

	@Mock
	private BestSellerRepository bestSellerRepository;

	@InjectMocks
	private BestSellerService bestSellerService;

	@Test // 이건 DB 구현체가 없으면 사실상 검증하기가 힘듦. Limit 때문에
	@DisplayName("입력한 기간(days)와 제한(limit)으로 베스트셀러를 조회한다.")
	void getTopBestSellersWithDaysAndLimit() {
		// given
		LocalDateTime fixedNow = LocalDateTime.of(2023, 10, 1, 0, 0);
		int days = 3;
		int limit = 5;

		List<BestSeller> mockResult = List.of(
			BestSeller.builder()
				.productName("상품 A")
				.description("설명 A")
				.price(BigDecimal.valueOf(1000))
				.stock(10L)
				.salesCount(50L)
				.build()
		);
		when(bestSellerRepository.findTopBySalesCountSince(any(LocalDateTime.class), eq(limit)))
			.thenReturn(mockResult);

		// when
		List<BestSellerSimpleInfo> result = bestSellerService.getTopBestSellers(fixedNow, days, limit);

		// then
		assertAll(
			() -> assertThat(result).hasSize(1),
			() -> assertThat(result.get(0).productName()).isEqualTo("상품 A")
		);

		verify(bestSellerRepository).findTopBySalesCountSince(any(LocalDateTime.class), eq(limit));

	}
}