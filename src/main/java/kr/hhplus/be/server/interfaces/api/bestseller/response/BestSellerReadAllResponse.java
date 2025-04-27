package kr.hhplus.be.server.interfaces.api.bestseller.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.domain.bestseller.dto.BestSellerSimpleInfo;

@Schema(name = "베스트셀러 목록 조회 응답")
public record BestSellerReadAllResponse(
	@Schema(name = "베스트셀러 목록", description = "베스트셀러 목록")
	List<BestSellerSimpleInfo> bestSellers
) {
}
