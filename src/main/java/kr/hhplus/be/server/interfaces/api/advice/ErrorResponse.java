package kr.hhplus.be.server.interfaces.api.advice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
	int code,
	String message,
	String detail,
	Map<String, String> validationErrors
) {

	public ErrorResponse {
		if (validationErrors == null)
			validationErrors = new HashMap<>();
	}

	public static ErrorResponse of(HttpStatus httpStatus, String message, String detail) {
		return new ErrorResponse(httpStatus.value(), message, detail, new HashMap<>());
	}

	public void addValidation(String fieldName, String errorMessage) {
		validationErrors.put(fieldName, errorMessage);
	}
}
