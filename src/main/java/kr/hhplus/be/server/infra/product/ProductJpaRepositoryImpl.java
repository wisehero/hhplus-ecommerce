package kr.hhplus.be.server.infra.product;

import static kr.hhplus.be.server.domain.product.QProduct.*;

import java.math.BigDecimal;
import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.interfaces.api.product.request.ProductSearchCondition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductJpaRepositoryImpl implements ProductJpaRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Product> findProductsByCondition(ProductSearchCondition condition) {
		// TODO Auto-generated method stub
		return queryFactory.select(product)
			.from(product)
			.where(
				equlasProductName(condition.productName()),
				greaterThanPrice(condition.minPrice()),
				lessThanPrice(condition.maxPrice())
			).fetch();
	}

	private BooleanExpression equlasProductName(String productName) {
		if (productName == null) {
			return null;
		}
		return product.productName.eq(productName);
	}

	private BooleanExpression greaterThanPrice(BigDecimal price) {
		if (price == null) {
			return null;
		}
		return product.price.gt(price);
	}

	private BooleanExpression lessThanPrice(BigDecimal price) {
		if (price == null) {
			return null;
		}
		return product.price.lt(price);
	}
}
