package kr.hhplus.be.server.api.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("사용자는 상품 목록을 조회할 수 있다.")
	void getProducts() throws Exception {
		// expected
		mockMvc.perform(get("/api/v1/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.products").isArray())
			.andExpect(jsonPath("$.data.products[0].productId").value(1))
			.andExpect(jsonPath("$.data.products[0].productName").value("상품1"))
			.andExpect(jsonPath("$.data.products[0].price").value(10000))
			.andExpect(jsonPath("$.data.products[0].stock").value(10))
			.andExpect(jsonPath("$.data.products[1].productId").value(2))
			.andExpect(jsonPath("$.data.products[1].productName").value("상품2"))
			.andExpect(jsonPath("$.data.products[1].price").value(20000))
			.andExpect(jsonPath("$.data.products[1].stock").value(20));
	}

	@Test
	@DisplayName("사용자는 최근 3일간 가장 많이 팔린 베스트 셀러 상품 5개를 조회할 수 있다.")
	void getBestSellerLimitFive() throws Exception {
		// expected
		mockMvc.perform(get("/api/v1/products/best"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.bestSellers").isArray())
			.andExpect(jsonPath("$.data.bestSellers[0].productId").value(1))
			.andExpect(jsonPath("$.data.bestSellers[0].productName").value("상품1"))
			.andExpect(jsonPath("$.data.bestSellers[0].salesCount").value(100))
			.andExpect(jsonPath("$.data.bestSellers[0].stock").value(10))
			.andExpect(jsonPath("$.data.bestSellers[0].price").value(10000))
			.andExpect(jsonPath("$.data.bestSellers[1].productId").value(2))
			.andExpect(jsonPath("$.data.bestSellers[1].productName").value("상품2"))
			.andExpect(jsonPath("$.data.bestSellers[1].salesCount").value(200))
			.andExpect(jsonPath("$.data.bestSellers[1].stock").value(20))
			.andExpect(jsonPath("$.data.bestSellers[1].price").value(20000));
	}
}