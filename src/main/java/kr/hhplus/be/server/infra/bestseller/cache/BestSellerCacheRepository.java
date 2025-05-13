package kr.hhplus.be.server.infra.bestseller.cache;

import java.util.List;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerItem;

public interface BestSellerCacheRepository {

	void incrementScore(BestSellerItem item);

	List<String> getTodayTopProductNames(int limit);
}
