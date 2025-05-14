package kr.hhplus.be.server.infra.bestseller.redis;

import java.util.List;

import kr.hhplus.be.server.domain.bestseller.dto.BestSellerItem;

public interface BestSellerRedisRepository {

	void incrementScore(BestSellerItem item);

	List<String> getTodayTopProductNames(int limit);
}
