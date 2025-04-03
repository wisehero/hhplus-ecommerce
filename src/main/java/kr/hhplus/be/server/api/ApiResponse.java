package kr.hhplus.be.server.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
	int code,
	String message,
	T data
) {

	// 200 OK 응답
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(HttpStatus.OK.value(), "요청이 정상적으로 처리되었습니다.", data);
	}

	// 201 Created 응답
	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(HttpStatus.CREATED.value(), "리소스가 성공적으로 생성되었습니다.", data);
	}

	public static <T> ResponseEntity<ApiResponse<T>> toResponseEntity(ApiResponse<T> response, HttpStatus status) {
		return new ResponseEntity<>(response, status);
	}
}
