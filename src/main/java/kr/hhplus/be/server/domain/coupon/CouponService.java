package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import lombok.RequiredArgsConstructor;

// TODO : DTO로 파라미터로 묶어보기

@Service
@RequiredArgsConstructor
public class CouponService {

	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

	public UserCoupon getUserCouponById(Long userCouponId) {
		return userCouponRepository.findById(userCouponId);
	}

	@Transactional
	public void issueCouponToUser(Long couponId, Long userId) {
		Coupon coupon = couponRepository.findById(couponId);

		coupon.validateIssueAvailable(LocalDate.now());

		if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
			throw new CouponAlreadyIssuedException();
		}

		coupon.issue();
		UserCoupon userCoupon = UserCoupon.create(userId, coupon);
		userCouponRepository.save(userCoupon);
	}

	@Transactional
	public BigDecimal applyCouponDiscount(Long userId, Long userCouponId, BigDecimal originalPrice) {
		UserCoupon userCoupon = userCouponRepository.findById(userCouponId);

		userCoupon.validateUsable(userId, LocalDate.now());

		Coupon coupon = couponRepository.findById(userCoupon.getCouponId());
		BigDecimal discountedPrice = coupon.applyDiscount(originalPrice);

		userCoupon.markUsed();

		return discountedPrice;
	}

	@Transactional
	public void restoreUserCoupon(Long userCouponId) {
		UserCoupon userCoupon = userCouponRepository.findById(userCouponId);
		userCoupon.markUnused();
	}
}
