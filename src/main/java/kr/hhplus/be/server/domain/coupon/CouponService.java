package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;

	public PublishedCoupon getPublishedCouponById(Long publishedCouponId) {
		if (publishedCouponId == null) {
			throw new IllegalArgumentException("사용하려는 쿠폰 ID는 null일 수 없습니다.");
		}
		return couponRepository.findPublishedCouponBy(publishedCouponId);
	}

	@Transactional
	public void issueCoupon(CouponIssueCommand command) {
		if (couponRepository.existsPublishedCouponBy(command.userId(), command.couponId())) {
			throw new CouponAlreadyIssuedException();
		}

		Coupon coupon = couponRepository.findById(command.couponId());
		coupon.issue();

		Coupon savedCoupon = couponRepository.save(coupon);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(command.userId(), savedCoupon, LocalDate.now());
		couponRepository.savePublishedCoupon(publishedCoupon);
	}

	@Transactional
	public void restorePublishedCoupon(Long publishedCouponId) {
		if (publishedCouponId == null) {
			throw new IllegalArgumentException("복원하려는 쿠폰 ID는 null일 수 없습니다.");
		}

		PublishedCoupon publishedCoupon = couponRepository.findPublishedCouponBy(publishedCouponId);
		publishedCoupon.restore();
		couponRepository.savePublishedCoupon(publishedCoupon);
	}
}
