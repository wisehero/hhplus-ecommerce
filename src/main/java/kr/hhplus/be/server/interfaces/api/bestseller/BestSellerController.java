package kr.hhplus.be.server.interfaces.api.bestseller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerSimpleInfo;

@RestController
@RequestMapping("/api/v1/bestsellers")
public class BestSellerController implements BestSellerControllerSpec {

	@GetMapping("/best")
	public ApiResponse<BestSellerReadAllResponse> getBestSellerLimitFive() {
		BestSellerReadAllResponse response = new BestSellerReadAllResponse(
			List.of(
				new BestSellerSimpleInfo(1L, "상품1", 100L, 10L, BigDecimal.valueOf(10000)),
				new BestSellerSimpleInfo(2L, "상품2", 200L, 20L, BigDecimal.valueOf(20000))
			)
		);

		return ApiResponse.ok(response);
	}
}
