package kr.hhplus.be.server.api.coupon;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.api.coupon.request.CouponIssueRequest;

@WebMvcTest(CouponController.class)
class CouponControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("사용자는 자신이 보유한 쿠폰을 조회할 수 있다.")
	void getCoupons() throws Exception {
		// given
		Long userId = 1L;

		// when
		mockMvc.perform(get("/api/v1/coupons?userId=" + userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.coupons").isArray())
			.andExpect(jsonPath("$.data.coupons[0].couponId").value(1L))
			.andExpect(jsonPath("$.data.coupons[0].couponTitle").value("10% 할인 쿠폰"))
			.andExpect(jsonPath("$.data.coupons[0].discountType").value("RATE"))
			.andExpect(jsonPath("$.data.coupons[0].discountValue").value(10))
			.andExpect(jsonPath("$.data.coupons[0].startDate").value("2023-10-01"))
			.andExpect(jsonPath("$.data.coupons[0].endDate").value("2023-10-31"))
			.andExpect(jsonPath("$.data.coupons[1].couponId").value(2L))
			.andExpect(jsonPath("$.data.coupons[1].couponTitle").value("10000원 할인 쿠폰"))
			.andExpect(jsonPath("$.data.coupons[1].discountType").value("AMOUNT"))
			.andExpect(jsonPath("$.data.coupons[1].discountValue").value(10000))
			.andExpect(jsonPath("$.data.coupons[1].startDate").value("2023-10-01"))
			.andExpect(jsonPath("$.data.coupons[1].endDate").value("2023-10-31"));
	}

	@Test
	@DisplayName("사용자는 쿠폰 발급을 신청하면 발급받은 쿠폰의 Id를 확인할 수 있다.")
	void issueCoupon() throws Exception {
		// given
		CouponIssueRequest request = new CouponIssueRequest(1L, 1L);

		// when
		mockMvc.perform(post("/api/v1/coupons/issue")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());
	}

}