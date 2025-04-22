package kr.hhplus.be.server.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import kr.hhplus.be.server.domain.base.BaseTimeEntity;
import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.issuePolicy.CouponIssuePolicyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String couponName;

	private BigDecimal discountValue;

	@Enumerated(EnumType.STRING)
	private DiscountType discountType;

	@Enumerated(EnumType.STRING)
	private CouponIssuePolicyType issuePolicyType;

	private Long remainingCount;

	private LocalDate validFrom;

	private LocalDate validTo;

	@Version
	private Long version;

	private Coupon(
		String couponName,
		BigDecimal discountValue,
		DiscountType discountType,
		CouponIssuePolicyType issuePolicyType,
		Long remainingCount,
		LocalDate validFrom,
		LocalDate validTo
	) {
		if (discountType == DiscountType.FIXED && discountValue.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("고정 할인 금액은 0보다 커야 합니다.");
		}

		if (discountType == DiscountType.PERCENTAGE && (discountValue.compareTo(BigDecimal.ZERO) <= 0
			|| discountValue.compareTo(BigDecimal.valueOf(100)) > 0)) {
			throw new IllegalArgumentException("퍼센트 할인 비율은 0보다 크고 100보다 작아야 합니다.");
		}
		this.couponName = couponName;
		this.discountValue = discountValue;
		this.discountType = discountType;
		this.issuePolicyType = issuePolicyType;
		this.remainingCount = remainingCount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public static Coupon createUnlimited(
		String couponName,
		BigDecimal discountValue,
		DiscountType discountType,
		LocalDate validFrom,
		LocalDate validTo
	) {
		return new Coupon(
			couponName,
			discountValue,
			discountType,
			CouponIssuePolicyType.UNLIMITED,
			null,
			validFrom,
			validTo
		);
	}

	public static Coupon createLimited(
		String couponName,
		BigDecimal discountValue,
		DiscountType discountType,
		Long remainingCount,
		LocalDate validFrom,
		LocalDate validTo
	) {
		return new Coupon(
			couponName,
			discountValue,
			discountType,
			CouponIssuePolicyType.LIMITED,
			remainingCount,
			validFrom,
			validTo
		);
	}

	public void issue() {
		this.issuePolicyType.toPolicy().issue(this);
	}

	public void decreaseRemainingCount() {
		this.remainingCount--;
	}

	public CouponSnapshot toSnapShot() {
		return new CouponSnapshot(
			this.couponName,
			this.discountType,
			this.discountValue,
			this.validFrom,
			this.validTo
		);
	}
}
