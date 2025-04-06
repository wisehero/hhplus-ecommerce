package kr.hhplus.be.server.api.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.interfaces.api.point.PointController;
import kr.hhplus.be.server.interfaces.api.point.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.request.PointUseRequest;

@WebMvcTest(PointController.class)
class PointControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("사용자는 사용자 ID를 입력으로 넘겨 자신이 보유한 포인트를 조회할 수 있다.")
	void getPointsOfUser() throws Exception {
		// given
		Long userId = 123L;

		// when
		mockMvc.perform(get("/api/v1/points?userId=" + userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.balance").value(10000L));
	}

	@Test
	@DisplayName("사용자는 포인트 충전 요청을 통해 포인트를 충전할 수 있다.")
	void chargeUserPoints() throws Exception {
		// given
		Long userId = 123L;
		Long chargeAmount = 10000L;
		PointChargeRequest request = new PointChargeRequest(userId, chargeAmount);

		// when
		mockMvc.perform(post("/api/v1/points/charge")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.userId").value(userId))
			.andExpect(jsonPath("$.data.balance").value(20000L));
	}

	@Test
	@DisplayName("사용자는 포인트 사용 요청을 통해 주문 금액에 따른 결제를 수행할 수 있다.")
	void useUserPoints() throws Exception {
		// given
		Long orderId = 1L;

		// when
		mockMvc.perform(post("/api/v1/points/use")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(new PointUseRequest(orderId))))
			.andExpect(status().isNoContent());
	}

}