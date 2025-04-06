package kr.hhplus.be.server.common;

public abstract class BusinessException extends RuntimeException {
	private static final String DEFAULT_MESSAGE = "비즈니스 정책을 위반한 요청입니다.";

	private String detail;

	public BusinessException(String detail) {
		super(DEFAULT_MESSAGE);
		this.detail = detail;
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getDetail() {
		return detail;
	}
}
