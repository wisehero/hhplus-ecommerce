package kr.hhplus.be.server.interfaces.api.bestseller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.domain.bestseller.BestSellerService;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bestsellers")
@RequiredArgsConstructor
public class BestSellerController implements BestSellerControllerSpec {

	private final BestSellerService bestSellerService;

	@GetMapping("/best")
	public ApiResponse<BestSellerReadAllResponse> getBestSeller(
		@RequestParam("limit") int days,
		@RequestParam("offset") int limit
	) {

		List<BestSellerSimpleInfo> topBestSellers = bestSellerService.getTopBestSellers(LocalDateTime.now(), days,
			limit);

		return ApiResponse.ok(new BestSellerReadAllResponse(topBestSellers));
	}
}
