package kr.hhplus.be.server.api.order;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.api.ApiResponse;
import kr.hhplus.be.server.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.api.order.response.OrderCreateResponse;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderControllerSpec {

	@PostMapping
	public ApiResponse<OrderCreateResponse> createOrder(@RequestBody OrderCreateRequest request) {
		return ApiResponse.ok(
			new OrderCreateResponse(
				1L
			)
		);
	}

}
