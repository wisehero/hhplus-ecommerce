package kr.hhplus.be.server.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	int code,
	String message,
	String detail,
	Map<String, String> validationErrors
) {
	public static ErrorResponse of(HttpStatus httpStatus, String message, String detail) {
		return new ErrorResponse(httpStatus.value(), message, detail, null);
	}

	public static ErrorResponse of(HttpStatus httpStatus, String message, String detail,
		Map<String, String> validationErrors) {
		return new ErrorResponse(httpStatus.value(), message, detail, validationErrors);
	}

	public ErrorResponse addValidation(String fieldName, String errorMessage) {
		Map<String, String> updatedMap =
			this.validationErrors != null ? new HashMap<>(this.validationErrors) : new HashMap<>();
		updatedMap.put(fieldName, errorMessage);
		return new ErrorResponse(code, message, detail, updatedMap);
	}
}
