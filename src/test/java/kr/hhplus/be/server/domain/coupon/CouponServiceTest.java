package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyUsedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponDoesNotBelongToUserException;
import kr.hhplus.be.server.domain.coupon.exception.CouponExpiredException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private UserCouponRepository userCouponRepository;

	@InjectMocks
	private CouponService couponService;

	private final Long couponId = 1L;
	private final Long userId = 100L;
	private final Long userCouponId = 99L;

	@Test
	@DisplayName("LIMITED 정책의 쿠폰이 정상적으로 발급되면 잔여 수량이 감소한다.")
	void issueCouponToUser() {
		// given
		Coupon limitedCoupon = Coupon.createLimitedCoupon(
			"테스트 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			10L,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);
		when(couponRepository.findById(couponId)).thenReturn(limitedCoupon);
		when(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).thenReturn(false);

		// when
		couponService.issueCouponToUser(couponId, userId);

		// then
		assertThat(limitedCoupon.getRemainingCount()).isEqualTo(9L);
		verify(couponRepository, times(1)).findById(couponId);
		verify(userCouponRepository, times(1)).existsByUserIdAndCouponId(userId, couponId);
		verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
	}

	@Test
	@DisplayName("UNLIMITED 정책의 쿠폰은 수량과 상관없이 발급되며 잔여 수량은 변하지 않는다.")
	void issueUnlimitedCouponDoesNotDecreaseRemainingCount() {
		// given
		Coupon unlimitedCoupon = Coupon.createUnlimitedCoupon(
			"무제한 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		when(couponRepository.findById(couponId)).thenReturn(unlimitedCoupon);
		when(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).thenReturn(false);

		// when
		couponService.issueCouponToUser(couponId, userId);

		// then
		assertThat(unlimitedCoupon.getRemainingCount()).isNull();

		verify(userCouponRepository).save(any(UserCoupon.class));
	}

	@Test
	@DisplayName("유효 기간이 지난 쿠폰은 발급될 수 없어 CouponExpiredException이 발생하고 쿠폰은 발급되지 않는다.")
	void issueFailsDueToExpiredCoupon() {
		// given
		Coupon expiredCoupon = Coupon.createLimitedCoupon(
			"만료 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(2000),
			10L,
			LocalDate.now().minusDays(10),
			LocalDate.now().minusDays(1) // 어제까지 유효
		);

		when(couponRepository.findById(couponId)).thenReturn(expiredCoupon);

		// expected
		assertThatThrownBy(() -> couponService.issueCouponToUser(couponId, userId))
			.isInstanceOf(CouponExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.".formatted(expiredCoupon.getEndDate()));
		verify(userCouponRepository, never()).save(any(UserCoupon.class));
	}

	@Test
	@DisplayName("LIMITED 정책의 쿠폰이 재고가 없으면 CouponOutOfStockException 예외가 발생하고 쿠폰은 발급되지 않는다.")
	void issueFailsDueToOutOfStock() {
		// given
		Coupon soldOutCoupon = Coupon.createLimitedCoupon(
			"품절 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			0L,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		when(couponRepository.findById(couponId)).thenReturn(soldOutCoupon);

		// expected
		assertThatThrownBy(() -> couponService.issueCouponToUser(couponId, userId))
			.isInstanceOf(CouponOutOfStockException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰 ID : %d는 모두 소진되었습니다.".formatted(soldOutCoupon.getId()));
		verify(userCouponRepository, never()).save(any(UserCoupon.class));
	}

	@Test
	@DisplayName("이미 발급받은 쿠폰일 경우 CouponAlreadyIssuedException 예외가 발생하고 쿠폰은 발급되지 않는다.")
	void issueFailsDueToAlreadyIssuedCoupon() {
		// given
		Coupon validCoupon = Coupon.createUnlimitedCoupon(
			"무제한 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(1000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		when(couponRepository.findById(couponId)).thenReturn(validCoupon);
		when(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> couponService.issueCouponToUser(couponId, userId))
			.isInstanceOf(CouponAlreadyIssuedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("이미 발급받은 쿠폰입니다.");
		verify(userCouponRepository, never()).save(any(UserCoupon.class));
	}

	@Test
	@DisplayName("쿠폰을 사용하면 쿠폰이 사용처리되고 할인된 금액이 반환된다.")
	void applyCouponDiscountSuccess() {
		// given
		BigDecimal originalPrice = BigDecimal.valueOf(10000);
		BigDecimal discountValue = BigDecimal.valueOf(10);

		Coupon coupon = Coupon.createUnlimitedCoupon(
			"10% 할인 쿠폰",
			DiscountType.PERCENTAGE,
			discountValue,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		UserCoupon unusedCoupon = UserCoupon.create(userId, coupon);

		when(userCouponRepository.findById(userCouponId)).thenReturn(unusedCoupon);
		when(couponRepository.findById(any())).thenReturn(coupon);

		// when
		BigDecimal discounted = couponService.applyCouponDiscount(userId, userCouponId, originalPrice);

		// then
		BigDecimal expected = originalPrice.multiply(BigDecimal.valueOf(0.9)); // 10% 할인
		assertAll(
			() -> assertThat(discounted).isEqualByComparingTo(expected),
			() -> assertThat(unusedCoupon.isUsed()).isTrue()
		);
	}

	@Test
	@DisplayName("쿠폰 사용시 쿠폰이 해당 사용자의 것이 아니라면 CouponDoesNotBelongToUserException 예외가 발생한다.")
	void applyCouponDiscountFailsWhenUserMismatch() {
		Long actualUserId = 99L;

		Coupon coupon = Coupon.createUnlimitedCoupon(
			"10% 할인 쿠폰",
			DiscountType.PERCENTAGE,
			BigDecimal.valueOf(10),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		UserCoupon someoneElsesCoupon = UserCoupon.create(actualUserId, coupon);

		when(userCouponRepository.findById(userCouponId)).thenReturn(someoneElsesCoupon);

		// expected
		assertThatThrownBy(() ->
			couponService.applyCouponDiscount(userId, userCouponId, BigDecimal.valueOf(10000))
		)
			.isInstanceOf(CouponDoesNotBelongToUserException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("사용자가 보유한 쿠폰이 아닙니다.");
	}

	@Test
	@DisplayName("쿠폰이 사용시 쿠폰이 만료되었다면 CouponExpiredException 예외가 발생한다.")
	void applyCouponDiscountFailsWhenCouponExpired() {
		// when
		Coupon expiredCoupon = Coupon.createUnlimitedCoupon(
			"만료된 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(3000),
			LocalDate.now().minusDays(10),
			LocalDate.now().minusDays(1)
		);

		UserCoupon expiredUserCoupon = UserCoupon.create(userId, expiredCoupon);

		when(userCouponRepository.findById(userCouponId)).thenReturn(expiredUserCoupon);

		// expected
		assertThatThrownBy(() ->
			couponService.applyCouponDiscount(userId, userCouponId, BigDecimal.valueOf(10000)))
			.isInstanceOf(CouponExpiredException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 만료되었습니다. 유효 기간은 %s까지 입니다.".formatted(expiredCoupon.getEndDate()));
	}

	@Test
	@DisplayName("쿠폰 사용시 이미 사용된 쿠폰이라면 CouponAlreadyUsedException 예외가 발생한다.")
	void applyCouponDiscountFailsWhenCouponAlreadyUsed() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"한 번만 쓸 수 있는 쿠폰",
			DiscountType.FIXED,
			BigDecimal.valueOf(5000),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		UserCoupon usedCoupon = UserCoupon.create(userId, coupon);
		usedCoupon.markUsed();

		when(userCouponRepository.findById(userCouponId)).thenReturn(usedCoupon);

		// expeceted
		assertThatThrownBy(() ->
			couponService.applyCouponDiscount(userId, userCouponId, BigDecimal.valueOf(10000))
		)
			.isInstanceOf(CouponAlreadyUsedException.class)
			.hasMessage("비즈니스 정책을 위반한 요청입니다.")
			.extracting("detail")
			.isEqualTo("쿠폰이 이미 사용되었습니다.");
	}

	@Test
	@DisplayName("쿠폰 사용 후 복원하면 쿠폰이 미사용 처리된다.")
	void restoreUserCoupon() {
		// given
		Coupon coupon = Coupon.createUnlimitedCoupon(
			"테스트 쿠폰",
			DiscountType.PERCENTAGE,
			BigDecimal.valueOf(10),
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(1)
		);

		UserCoupon usedCoupon = UserCoupon.create(1L, coupon);
		usedCoupon.markUsed(); // 쿠폰을 사용된 상태로 만든다

		when(userCouponRepository.findById(userCouponId)).thenReturn(usedCoupon);

		// when
		couponService.restoreUserCoupon(userCouponId);

		// then
		assertThat(usedCoupon.isUsed()).isFalse(); // 복원된 상태여야 함
	}
}