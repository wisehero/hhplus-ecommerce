package kr.hhplus.be.server.interfaces.api.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerReadAllResponse;
import kr.hhplus.be.server.interfaces.api.bestseller.response.BestSellerSimpleInfo;
import kr.hhplus.be.server.interfaces.api.product.response.ProductReadAllResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductSimpleInfo;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductControllerSpec {

	@GetMapping
	public ApiResponse<ProductReadAllResponse> getProducts() {
		ProductReadAllResponse response = new ProductReadAllResponse(
			List.of(
				new ProductSimpleInfo(1L, "상품1", BigDecimal.valueOf(10000), 10L),
				new ProductSimpleInfo(2L, "상품2", BigDecimal.valueOf(20000), 20L)
			)
		);
		return ApiResponse.ok(response);

	}
}
