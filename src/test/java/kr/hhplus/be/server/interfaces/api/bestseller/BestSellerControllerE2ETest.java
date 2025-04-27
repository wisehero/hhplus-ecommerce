package kr.hhplus.be.server.interfaces.api.bestseller;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import kr.hhplus.be.server.domain.bestseller.BestSeller;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;
import kr.hhplus.be.server.infra.bestseller.BestSellerJpaRepository;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import kr.hhplus.be.server.support.DbCleaner;
import kr.hhplus.be.server.support.E2ETestSupprot;

class BestSellerControllerE2ETest extends E2ETestSupprot {

	@LocalServerPort
	int port;

	@Autowired
	DbCleaner dbCleaner;

	@Autowired
	BestSellerJpaRepository bestSellerJpaRepository;

	RestClient restClient;

	@BeforeEach
	void setUp() {
		restClient = RestClient.builder()
			.baseUrl("http://localhost:" + port)
			.build();

		dbCleaner.execute();
	}

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
			.uri("/api/v1/bestsellers")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		// then
		assertAll(
			() -> assertThat(response.code()).isEqualTo(200),
			() -> assertThat(response.data().bestSellers().size()).isLessThanOrEqualTo(100)
		);
	}
}