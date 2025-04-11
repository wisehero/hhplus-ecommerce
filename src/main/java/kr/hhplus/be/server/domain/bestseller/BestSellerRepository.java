package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BestSellerRepository {

	List<BestSeller> findTopBySalesCountSince(LocalDate from, int limit);

}
