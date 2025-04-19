package kr.hhplus.be.server.interfaces.api.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.server.application.order.dto.OrderCreateResult;
import kr.hhplus.be.server.application.order.facade.OrderFacade;
import kr.hhplus.be.server.interfaces.api.ApiResponse;
import kr.hhplus.be.server.interfaces.api.order.request.OrderCreateRequest;
import kr.hhplus.be.server.interfaces.api.order.response.OrderCreateResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerSpec {

	private final OrderFacade orderFacade;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<OrderCreateResponse> createOrder(@RequestBody OrderCreateRequest request) {
		OrderCreateResult result = orderFacade.createOrder(request.toOrderCreateCommand());

		return ApiResponse.created(new OrderCreateResponse(result.orderId()));
	}

}
