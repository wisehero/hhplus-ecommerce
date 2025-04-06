package kr.hhplus.be.server.interfaces.api.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.interfaces.api.order.response.OrderCreateResponse;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderControllerSpec {

	@PostMapping
	public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(@RequestBody OrderCreateRequest request) {
		ApiResponse<OrderCreateResponse> response = ApiResponse.created(new OrderCreateResponse(1L));
		return ApiResponse.toResponseEntity(response);
	}

}
