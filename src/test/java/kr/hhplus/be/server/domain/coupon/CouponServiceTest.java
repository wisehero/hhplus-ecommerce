package kr.hhplus.be.server.domain.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.coupon.discountpolicy.DiscountType;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	@Mock
	private CouponRepository couponRepository;

	@InjectMocks
	private CouponService couponService;

	@Test
	@DisplayName("발급된 쿠폰 조회 시 유효한 쿠폰 ID가 주어졌을 때 PublishedCoupon을 반환한다.")
	void getPublishedCouponByIdWithValidIdShouldReturnCoupon() {
		// given
		Long publishedCouponId = 1L;

		PublishedCoupon expected = Instancio.of(PublishedCoupon.class)
			.set(Select.field("id"), publishedCouponId)
			.create();

		when(couponRepository.findPublishedCouponById(publishedCouponId)).thenReturn(expected);

		// when
		PublishedCoupon result = couponService.getPublishedCouponById(publishedCouponId);

		// then
		assertAll(
			() -> assertThat(result).isEqualTo(expected),
			() -> assertThat(result.getId()).isEqualTo(publishedCouponId)
		);
		verify(couponRepository).findPublishedCouponById(publishedCouponId);
	}

	@Test
	@DisplayName("발급된 쿠폰 조회 시 쿠폰 ID가 null이면 IllegalArgumentException이 발생한다.")
	void getPublishedCouponByIdWithNullIdShouldThrow() {
		// when & then
		assertThatThrownBy(() -> couponService.getPublishedCouponById(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("사용하려는 쿠폰 ID는 null일 수 없습니다.");

		verify(couponRepository, never()).findPublishedCouponById(any());
	}

	@Test
	@DisplayName("LIMITED 정책의 쿠폰이 정상적으로 발급되면 잔여 수량이 감소한다.")
	void issueCouponToUser() {
		Coupon coupon = Coupon.createLimited(
			"테스트 쿠폰",
			BigDecimal.valueOf(3000),
			DiscountType.FIXED,
			5L,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(7)
		);

		CouponIssueCommand command = new CouponIssueCommand(0L, 0L);

		when(couponRepository.existsPublishedCouponBy(anyLong(), anyLong())).thenReturn(false);
		when(couponRepository.findById(anyLong())).thenReturn(coupon);
		when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		couponService.issueCoupon(command);

		// then
		assertAll(
			() -> assertThat(coupon.getRemainingCount()).isEqualTo(4L),
			() -> verify(couponRepository).existsPublishedCouponBy(anyLong(), anyLong()),
			() -> verify(couponRepository).findById(anyLong()),
			() -> verify(couponRepository).save(any(Coupon.class)),
			() -> verify(couponRepository).savePublishedCoupon(any(PublishedCoupon.class))
		);
	}

	@Test
	@DisplayName("UNLIMITED 정책의 쿠폰은 수량과 상관없이 발급되며 잔여 수량은 변하지 않는다.")
	void issueUnlimitedCouponDoesNotDecreaseRemainingCount() {
		// given
		Coupon coupon = Coupon.createUnlimited(
			"무제한 쿠폰",
			BigDecimal.valueOf(5000),
			DiscountType.FIXED,
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(7)
		);

		CouponIssueCommand command = new CouponIssueCommand(0L, 0L);

		when(couponRepository.existsPublishedCouponBy(anyLong(), anyLong())).thenReturn(false);
		when(couponRepository.findById(anyLong())).thenReturn(coupon);
		when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		couponService.issueCoupon(command);

		// then
		assertAll(
			() -> assertThat(coupon.getRemainingCount()).isNull(),
			() -> verify(couponRepository).existsPublishedCouponBy(anyLong(), anyLong()),
			() -> verify(couponRepository).findById(anyLong()),
			() -> verify(couponRepository).save(any(Coupon.class)),
			() -> verify(couponRepository).savePublishedCoupon(any(PublishedCoupon.class))
		);

	}

	@Test
	@DisplayName("LIMITED 정책의 쿠폰이 재고가 없으면 CouponOutOfStockException 예외가 발생하고 쿠폰은 발급되지 않는다.")
	void issueFailsDueToOutOfStock() {
		// given
		Coupon coupon = Coupon.createLimited(
			"품절 쿠폰",
			BigDecimal.valueOf(3000),
			DiscountType.FIXED,
			0L, // 재고 없음
			LocalDate.now().minusDays(1),
			LocalDate.now().plusDays(7)
		);

		CouponIssueCommand command = new CouponIssueCommand(0L, 0L);

		when(couponRepository.existsPublishedCouponBy(anyLong(), anyLong())).thenReturn(false);
		when(couponRepository.findById(anyLong())).thenReturn(coupon);

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(command))
			.isInstanceOf(CouponOutOfStockException.class);

		verify(couponRepository, never()).save(any());
		verify(couponRepository, never()).savePublishedCoupon(any());
	}

	@Test
	@DisplayName("이미 발급받은 쿠폰일 경우 CouponAlreadyIssuedException 예외가 발생하고 쿠폰은 발급되지 않는다.")
	void issueFailsDueToAlreadyIssuedCoupon() {
		// given
		CouponIssueCommand command = new CouponIssueCommand(0L, 0L); // ID는 의미 없음

		when(couponRepository.existsPublishedCouponBy(anyLong(), anyLong())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> couponService.issueCoupon(command))
			.isInstanceOf(CouponAlreadyIssuedException.class);

		// 이후 로직은 호출되지 않아야 함
		verify(couponRepository, never()).findById(any());
		verify(couponRepository, never()).save(any());
		verify(couponRepository, never()).savePublishedCoupon(any());
	}

	@Test
	@DisplayName("사용된 쿠폰을 복원하면 isUsed 상태가 false로 변경되고 저장된다.")
	void shouldRestoreUsedCouponSuccessfully() {
		// given
		PublishedCoupon publishedCoupon = PublishedCoupon.create(
			1L,
			Coupon.createUnlimited(
				"무제한 쿠폰",
				BigDecimal.valueOf(1000),
				DiscountType.FIXED,
				LocalDate.now().minusDays(1),
				LocalDate.now().plusDays(7)
			),
			LocalDate.now()
		);

		publishedCoupon.discount(BigDecimal.valueOf(10000), LocalDate.now()); // 사용 처리

		when(couponRepository.findPublishedCouponById(anyLong())).thenReturn(publishedCoupon);

		// when
		couponService.restorePublishedCoupon(0L); // ID는 중요하지 않음

		// then
		assertAll(
			() -> assertThat(publishedCoupon.isUsed()).isFalse(),
			() -> verify(couponRepository).findPublishedCouponById(anyLong()),
			() -> verify(couponRepository).savePublishedCoupon(publishedCoupon)
		);
	}

	@Test
	@DisplayName("쿠폰 ID가 null이면 예외가 발생하고 복원은 수행되지 않는다.")
	void shouldThrowExceptionWhenCouponIdIsNull() {
		// when & then
		assertThatThrownBy(() -> couponService.restorePublishedCoupon(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("복원하려는 쿠폰 ID는 null일 수 없습니다.");

		verify(couponRepository, never()).findPublishedCouponById(any());
		verify(couponRepository, never()).savePublishedCoupon(any());
	}
}