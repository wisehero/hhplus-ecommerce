package kr.hhplus.be.server.domain.bestseller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BestSellerService {

	private final BestSellerRepository bestSellerRepository;

	public List<BestSeller> getTopBestSellers(int days, int limit) {
		LocalDate from = LocalDate.now().minusDays(days);
		return bestSellerRepository.findTopBySalesCountSince(from, limit);
	}
}
