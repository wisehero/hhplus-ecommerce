package kr.hhplus.be.server.api.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.interfaces.api.order.OrderController;
import kr.hhplus.be.server.interfaces.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.interfaces.api.order.request.OrderProduct;

@WebMvcTest(OrderController.class)
class OrderControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("사용자는 쿠폰, 상품을 선택하여 주문을 생성할 수 있다.")
	void createOrder() throws Exception {
		// given
		OrderCreateRequest request = new OrderCreateRequest(
			1L,
			2L,
			List.of(
				new OrderProduct(1L, 10)
			));

		// when
		mockMvc.perform(post("/api/v1/orders")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.orderId").value(1L));
	}

	@Test
	@DisplayName("사용자는 쿠폰 없이 상품만 선택하여 주문을 생성할 수 있다.")
	void createOrderWithoutCoupon() throws Exception {
		// given
		OrderCreateRequest request = new OrderCreateRequest(
			1L,
			null,
			List.of(
				new OrderProduct(1L, 10)
			));

		// when
		mockMvc.perform(post("/api/v1/orders")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.orderId").value(1L));
	}

}