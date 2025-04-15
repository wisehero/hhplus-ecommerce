package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponDoesNotUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "published_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PublishedCoupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private Long couponId;

	@Embedded
	private CouponSnapshot couponSnapshot;

	private boolean isUsed;

	private LocalDate issuedAt;

	private PublishedCoupon(Long userId, Long couponId, CouponSnapshot couponSnapshot, boolean isUsed,
		LocalDate issuedAt) {
		this.userId = userId;
		this.couponId = couponId;
		this.couponSnapshot = couponSnapshot;
		this.isUsed = isUsed;
		this.issuedAt = issuedAt;
	}

	public static PublishedCoupon create(Long userId, Coupon coupon, LocalDate issuedAt) {
		return new PublishedCoupon(userId, coupon.getId(), coupon.toSnapShot(), false, issuedAt);
	}

	public BigDecimal discount(BigDecimal originalPrice, LocalDate now) {
		if (isUsed) {
			throw new CouponAlreadyUsedException();
		}

		if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("가격이 null이거나 0 이하입니다.");
		}

		if (now.isBefore(couponSnapshot.validFrom()) || now.isAfter(couponSnapshot.validTo())) {
			throw new CouponExpiredException(couponSnapshot.validTo());
		}

		this.isUsed = true;
		return this.couponSnapshot.discountType()
			.toPolicy(couponSnapshot.discountValue())
			.apply(originalPrice);
	}

	public void restore() {
		if (!isUsed) {
			throw new CouponDoesNotUsedException();
		}
		this.isUsed = false;
	}
}
