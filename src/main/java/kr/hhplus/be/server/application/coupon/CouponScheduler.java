package kr.hhplus.be.server.application.coupon;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.PublishedCoupon;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

	private static final int BATCH_SIZE = 100;
	private final CouponRepository couponRepository;

	@Scheduled(fixedRate = 1000)
	@Transactional
	public void processCouponQueue() {
		Set<Long> couponIds = couponRepository.getAllCouponIds();

		for (Long couponId : couponIds) {
			Coupon coupon = couponRepository.findById(couponId);

			// 재고가 없으면 건너뛰기 (대기열은 전부 삭제)
			if (coupon.getRemainingCount() <= 0) {
				Set<String> userIds = couponRepository.getNextBatchFromQueue(couponId, BATCH_SIZE, 0);
				couponRepository.removeFromQueue(couponId, userIds);
				continue;
			}

			// 실제 남은 재고만큼만 처리
			int availableStock = coupon.getRemainingCount().intValue();
			Set<String> userIds = couponRepository.getNextBatchFromQueue(couponId, BATCH_SIZE, availableStock);

			if (userIds == null || userIds.isEmpty()) {
				continue;
			}

			// 발급 처리

			List<PublishedCoupon> publishedCoupons = userIds.stream()
				.map(userId -> PublishedCoupon.create(Long.parseLong(userId), coupon, LocalDate.now()))
				.toList();

			// RDB 저장
			couponRepository.saveAll(publishedCoupons);

			// 대기열에서 제거
			couponRepository.removeFromQueue(couponId, userIds);
		}
	}
}
