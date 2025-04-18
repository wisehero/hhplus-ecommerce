package kr.hhplus.be.server.interfaces.api.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductReadAllResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductSimpleInfo;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerSpec {

	private final ProductService productService;

	@GetMapping
	public ApiResponse<ProductReadAllResponse> getProducts(
		@RequestParam(required = false) String name,
		@RequestParam(required = false) BigDecimal minPrice,
		@RequestParam(required = false) BigDecimal maxPrice) {
		List<ProductSimpleInfo> productSimpleInfos = productService.getAllProducts()
			.stream()
			.map(ProductSimpleInfo::of)
			.toList();
		return ApiResponse.ok(new ProductReadAllResponse(productSimpleInfos));
	}
}
