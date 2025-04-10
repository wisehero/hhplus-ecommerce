package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountPolicy;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicy;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon extends BaseTimeEntity {

	@Id
	private Long id;

	private String couponName;

	private BigDecimal discountValue;

	private DiscountType discountType;

	private CouponIssuePolicyType issuePolicyType;

	private Long remainingCount;

	private LocalDate startDate;
	private LocalDate endDate;

	private Coupon(
		String couponName,
		DiscountType discountType,
		BigDecimal discountValue,
		CouponIssuePolicyType issuePolicyType,
		Long remainingCount,
		LocalDate startDate,
		LocalDate endDate
	) {
		validateDiscountValue(discountType, discountValue);
		this.couponName = couponName;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.issuePolicyType = issuePolicyType;
		this.remainingCount = remainingCount;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public static Coupon createLimitedCoupon(
		String couponName,
		DiscountType discountType,
		BigDecimal discountValue,
		Long remainingCount,
		LocalDate startDate,
		LocalDate endDate
	) {
		return new Coupon(
			couponName,
			discountType,
			discountValue,
			CouponIssuePolicyType.LIMITED,
			remainingCount,
			startDate,
			endDate
		);
	}

	public static Coupon createUnlimitedCoupon(
		String couponName,
		DiscountType discountType,
		BigDecimal discountValue,
		LocalDate startDate,
		LocalDate endDate
	) {
		return new Coupon(
			couponName,
			discountType,
			discountValue,
			CouponIssuePolicyType.UNLIMITED,
			null,
			startDate,
			endDate
		);
	}

	public BigDecimal applyDiscount(BigDecimal originalPrice) {
		DiscountPolicy policy = discountType.toPolicy(discountValue);
		return policy.apply(originalPrice);
	}

	private boolean isInValidPeriod(LocalDate now) {
		return !(now.isBefore(startDate) || now.isAfter(endDate));
	}

	public void validateIssueAvailable(LocalDate now) {
		if (!isInValidPeriod(now)) {
			throw new CouponExpiredException(this.endDate);
		}
		if (!canIssue()) {
			throw new CouponOutOfStockException(this.id);
		}
	}

	public void issue() {
		this.issuePolicyType.toPolicy().issue(this);
	}

	private CouponIssuePolicy getIssuePolicy() {
		return issuePolicyType.toPolicy();
	}

	public void decreaseRemainingCount() {
		this.remainingCount -= 1;
	}

	private boolean canIssue() {
		return this.issuePolicyType.toPolicy().canIssue(this);
	}

	private void validateDiscountValue(DiscountType discountType, BigDecimal discountValue) {
		if (discountType == null)
			throw new IllegalArgumentException("할인 타입은 필수입니다.");

		if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("할인 값은 0보다 커야 합니다.");
		}
		if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
			throw new IllegalArgumentException("정률 할인은 100%를 넘을 수 없습니다.");
		}
	}
}
