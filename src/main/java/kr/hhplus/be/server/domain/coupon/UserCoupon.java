package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponDoesNotBelongToUserException;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseTimeEntity {

	@Id
	private Long id;

	private Long userId;

	private Long couponId;

	private String couponName;

	private boolean isUsed;

	private LocalDate issuedAt;

	private LocalDate expireAt;

	private UserCoupon(Long userId, Long couponId, String couponName, LocalDate issuedAt, LocalDate expiredAt) {
		this.userId = userId;
		this.couponId = couponId;
		this.couponName = couponName;
		this.isUsed = false;
		this.issuedAt = issuedAt;
		this.expireAt = expiredAt;
	}

	public static UserCoupon createUserCoupon(Long userId, Long couponId, String couponName, LocalDate issuedAt,
		LocalDate expiredAt) {
		return new UserCoupon(userId, couponId, couponName, issuedAt, expiredAt);
	}

	public static UserCoupon create(Long userId, Coupon coupon) {
		return new UserCoupon(
			userId,
			coupon.getId(),
			coupon.getCouponName(),
			LocalDate.now(),
			coupon.getEndDate()
		);
	}

	public void validateUsable(Long expectedUserId, LocalDate now) {
		if (!this.userId.equals(expectedUserId)) {
			throw new CouponDoesNotBelongToUserException();
		}

		if (this.isUsed) {
			throw new CouponAlreadyUsedException();
		}
		if (isExpired(now)) {
			throw new CouponExpiredException(this.expireAt);
		}
	}

	public void markUsed() {
		if (this.isUsed) {
			throw new CouponAlreadyUsedException();
		}
		this.isUsed = true;
	}

	public void markUnused() {
		if (!this.isUsed)
			return;
		this.isUsed = false;
	}

	private boolean isExpired(LocalDate now) {
		return now.isAfter(this.expireAt);
	}
}
