package kr.hhplus.be.server.interfaces.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssueCommand;
import kr.hhplus.be.server.domain.coupon.event.type.CouponIssueRequestEvent;
import kr.hhplus.be.server.domain.coupon.exception.CouponAlreadyIssuedException;
import kr.hhplus.be.server.domain.coupon.exception.CouponOutOfStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponKafkaConsumer {

	private final CouponService couponService;
	private static final String TOPIC_COUPON_ISSUE = "coupon-issue";

	@KafkaListener(topics = TOPIC_COUPON_ISSUE, groupId = "coupon-issue-group")
	public void consume(CouponIssueRequestEvent event,
		@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
		@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
		@Header(KafkaHeaders.OFFSET) String offset) {

		try {
			log.info("쿠폰 발급 요청 이벤트 수신: {}, topic: {}, partition: {}, offset: {}", event, topic, partition, offset);

			couponService.issueCoupon(
				new CouponIssueCommand(
					event.userId(),
					event.couponId()));

			log.info("쿠폰 발급 완료: {}, topic: {}, partition: {}, offset: {}", event, topic, partition, offset);
		} catch (CouponOutOfStockException e) {
			// 쿠폰 재고 부족은 정상적인 실패임
			log.info("쿠폰 재고 부족으로 발급 실패 : offset={}, message={}", offset, e.getMessage());

		} catch (CouponAlreadyIssuedException e) {
			// 쿠폰 중복 발급은 정상적인 실패임
			log.info("중복 발급 시도: offset={}, userId={}", offset, event.userId());

		} catch (Exception e) {
			log.error("쿠폰 발급 처리 중 오류 발생: {}, topic: {}, partition: {}, offset: {}", event, topic, partition, offset, e);
			// 실패 이벤트들을 모아두는 별도의 저장소에 저장하는 로직
		}
	}
}
