package kr.hhplus.be.server.interfaces.api.bestseller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerSimpleInfo;

@WebMvcTest(BestSellerController.class)
class BestSellerControllerUnitTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BestSellerService bestSellerService;

	@Test
	@DisplayName("n일간 가장 많이 팔린 m개의 상품인기 상품 목록을 조회할 수 있다.")
	void getBestSellers() throws Exception {
		// given
		int days = 7;
		int limit = 10;

		List<BestSeller> mockList = IntStream.range(1, limit)
			.mapToObj(i -> BestSeller.builder()
				.productName("상품 " + i)
				.description("설명 " + i)
				.price(BigDecimal.valueOf(1000L * i))
				.stock(100L + i)
				.salesCount(10L * i)
				.build())
			.toList();

		when(bestSellerService.getTopBestSellers(days, limit)).thenReturn(mockList);

		// when
		mockMvc.perform(get("/api/v1/bestsellers")
				.param("days", String.valueOf(days))
				.param("limit", String.valueOf(limit)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.bestSellers").isArray())
			.andExpect(jsonPath("$.data.bestSellers.length()").value(mockList.size()));

		verify(bestSellerService, times(1)).getTopBestSellers(days, limit);
	}
}