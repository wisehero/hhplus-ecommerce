package kr.hhplus.be.server.interfaces.api.bestseller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerResult;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.constants.BestSellerPeriod;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bestsellers")
@RequiredArgsConstructor
public class BestSellerController implements BestSellerControllerSpec {

	private final BestSellerService bestSellerService;

	@GetMapping
	public ApiResponse<BestSellerReadAllResponse> getBestSeller(
		@RequestParam(value = "period") BestSellerPeriod period
	) {
		BestSellerResult result = switch (period) {
			case DAILY -> bestSellerService.getTopBestSellersDaily();
			case WEEKLY -> bestSellerService.getTopBestSellersWeekly();
			case MONTHLY -> bestSellerService.getTopBestSellersMonthly();
		};

		return ApiResponse.ok(new BestSellerReadAllResponse(result));
	}

	@GetMapping("/realtime")
	public ApiResponse<List<String>> getTodayRealTimeRanking(
		@RequestParam("limit") int limit
	) {
		return ApiResponse.ok(bestSellerService.getTodayRealTimeRankingProductNamesWithLimit(limit));
	}
}
