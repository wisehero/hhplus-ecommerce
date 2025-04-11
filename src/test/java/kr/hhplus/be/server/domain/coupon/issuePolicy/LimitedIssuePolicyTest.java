package kr.hhplus.be.server.domain.coupon.issuePolicy;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;

class LimitedIssuePolicyTest {

	private final LimitedIssuePolicy policy = new LimitedIssuePolicy();

	@Test
	@DisplayName("쿠폰의 잔여 수량이 1 이상이면 발급 가능하다")
	void canIssueWhenRemainingCountPositive() {
		// given
		Coupon coupon = mock(Coupon.class);
		when(coupon.getRemainingCount()).thenReturn(3L);

		// expected
		assertThat(policy.canIssue(coupon)).isTrue();
	}

	@Test
	@DisplayName("쿠폰의 잔여 수량이 0일 경우 발급은 불가능하고, issue 시 예외가 발생한다")
	void cannotIssueWhenRemainingIsZeroAndThrowsExceptionOnIssue() {
		// given
		Coupon coupon = mock(Coupon.class);
		when(coupon.getRemainingCount()).thenReturn(0L);

		// expecetd
		assertAll(
			() -> assertThat(policy.canIssue(coupon)).isFalse(),
			() -> assertThatThrownBy(() -> policy.issue(coupon))
				.isInstanceOf(CouponOutOfStockException.class)
		);
	}

	@Test
	@DisplayName("발급 시 잔여 수량이 있다면 decreaseRemainingCount()가 호출된다")
	void issueDecreasesRemainingCount() {
		// given
		Coupon coupon = mock(Coupon.class);
		when(coupon.getRemainingCount()).thenReturn(5L);

		// when
		policy.issue(coupon);

		// expected
		verify(coupon, times(1)).decreaseRemainingCount();
	}
}