package kr.hhplus.be.server.interfaces.api.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.product.request.ProductSearchCondition;
import kr.hhplus.be.server.interfaces.api.product.response.ProductReadAllResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductSimpleInfo;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerSpec {

	private final ProductService productService;

	@GetMapping
	public ApiResponse<ProductReadAllResponse> getProducts(ProductSearchCondition condition) {
		System.out.println(condition);
		List<ProductSimpleInfo> productSimpleInfos = productService.getProductsByCondition(condition)
			.stream()
			.map(ProductSimpleInfo::of)
			.toList();
		return ApiResponse.ok(new ProductReadAllResponse(productSimpleInfos));
	}
}
