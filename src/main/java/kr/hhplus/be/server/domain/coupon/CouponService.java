package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDate;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.support.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;

	public PublishedCoupon getPublishedCouponById(Long publishedCouponId) {
		if (publishedCouponId == null) {
			throw new IllegalArgumentException("사용하려는 쿠폰 ID는 null일 수 없습니다.");
		}
		return couponRepository.findPublishedCouponById(publishedCouponId);
	}

	// 쿠폰 발급 LockFree
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

	// 	쿠폰 발급 : 비관적 락 사용
	@Transactional
	public void issueCouponV2(CouponIssueCommand command) {

		Coupon coupon = couponRepository.findByIdWithPessimistic(command.couponId());

		if (couponRepository.existsPublishedCouponBy(command.userId(), command.couponId())) {
			throw new CouponAlreadyIssuedException();
		}

		coupon.issue();

		Coupon savedCoupon = couponRepository.save(coupon);

		PublishedCoupon publishedCoupon = PublishedCoupon.create(command.userId(), savedCoupon, LocalDate.now());
		couponRepository.savePublishedCoupon(publishedCoupon);
	}

	@DistributedLock(key = "'coupon:' + #command.couponId")
	@Transactional
	public void issueCouponV4(CouponIssueCommand command) {
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

		PublishedCoupon publishedCoupon = couponRepository.findPublishedCouponById(publishedCouponId);
		publishedCoupon.restore();
		couponRepository.savePublishedCoupon(publishedCoupon);
	}
}
