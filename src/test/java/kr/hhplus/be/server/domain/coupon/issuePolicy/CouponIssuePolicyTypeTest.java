package kr.hhplus.be.server.domain.coupon.issuePolicy;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.Coupon;

class CouponIssuePolicyTypeTest {

	private final Coupon coupon = mock(Coupon.class);

	@Test
	@DisplayName("UNLIMITED 타입은 UnlimitedIssuePolicy를 반환한다")
	void toPolicyReturnsUnlimitedPolicy() {
		CouponIssuePolicy policy = CouponIssuePolicyType.UNLIMITED.toPolicy();

		assertAll(
			() -> assertThat(policy).isInstanceOf(UnlimitedIssuePolicy.class),
			() -> assertThat(policy.canIssue(coupon)).isTrue()
		);
	}

	@Test
	@DisplayName("LIMITED 타입은 LimitedIssuePolicy를 반환한다")
	void toPolicyReturnsLimitedPolicy() {
		CouponIssuePolicy policy = CouponIssuePolicyType.LIMITED.toPolicy();

		when(coupon.getRemainingCount()).thenReturn(1L);

		assertAll(
			() -> assertThat(policy).isInstanceOf(LimitedIssuePolicy.class),
			() -> assertThat(policy.canIssue(coupon)).isTrue()
		);
	}
}