package kr.hhplus.be.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
		log.info("IllegalArgumentException: {}", e.getMessage(), e);
		return ErrorResponse.of(
			HttpStatus.BAD_REQUEST,
			"유효하지 않은 입력을 포함한 요청입니다.",
			e.getMessage());
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBindException(org.springframework.validation.BindException e) {
		log.info("BindException: {}", e.getMessage(), e);
		ErrorResponse response = ErrorResponse.of(
			HttpStatus.BAD_REQUEST,
			"유효성 검사 실패",
			"한 개 이상의 필드가 유효하지 않습니다."
		);

		for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			response = response.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return response;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		log.info("MethodArgumentTypeMismatchException: {}", e.getMessage(), e);

		String parameterName = e.getParameter().getParameterName();
		Object value = e.getValue();
		String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s.", value,
			parameterName, expectedType);

		return ErrorResponse.of(
			HttpStatus.BAD_REQUEST,
			"Invalid Request Parameter",
			message);
	}

	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleBusinessException(BusinessException e) {
		return ErrorResponse.of(
			HttpStatus.CONFLICT,
			e.getMessage(),
			e.getDetail());
	}

	@ExceptionHandler(Exception.class)
	public ErrorResponse handleException(Exception e) {
		log.error("Exception: {}", e.getMessage(), e);
		return ErrorResponse.of(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Internal Server Error",
			"핸들링되지 않은 예외입니다.");
	}
}
