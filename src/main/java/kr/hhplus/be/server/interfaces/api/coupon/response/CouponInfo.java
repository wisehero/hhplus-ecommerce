package kr.hhplus.be.server.interfaces.api.coupon.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.builder.HashCodeExclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 정보")
public record CouponInfo(

	@Schema(description = "쿠폰 ID", example = "1")
	Long couponId,
	@Schema(description = "쿠폰 이름", example = "10000원 할인 쿠폰")
	String couponTitle,
	@Schema(description = "쿠폰 타입(RATE, AMOUNT)", example = "10000원 할인 쿠폰")
	String discountType,
	@Schema(description = "쿠폰 할인율", example = "10000")
	BigDecimal discountValue,
	@Schema(description = "쿠폰 사용 가능 시작 날짜", example = "2023-10-01")
	LocalDate startDate,
	@Schema(description = "쿠폰 사용 가능 종료 날짜", example = "2023-10-31")
	LocalDate endDate
) {
}
