package kr.hhplus.be.server.domain.coupon.issuePolicy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.hhplus.be.server.domain.coupon.Coupon;

class UnlimitedIssuePolicyTest {

	private final Coupon coupon = mock(Coupon.class);

	private final UnlimitedIssuePolicy policy = new UnlimitedIssuePolicy();

	@Test
	@DisplayName("무제한 쿠폰은 항상 발급 가능하다")
	void canIssueAlwaysReturnsTrue() {
		assertThat(policy.canIssue(coupon)).isTrue();
	}

	@Test
	@DisplayName("무제한 쿠폰은 발급 시에도 아무 동작을 하지 않는다")
	void issueDoesNothing() {
		assertThatCode(() -> policy.issue(coupon))
			.doesNotThrowAnyException();
	}
}