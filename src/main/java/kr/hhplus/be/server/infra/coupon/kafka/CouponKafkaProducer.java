package kr.hhplus.be.server.infra.coupon.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.coupon.event.CouponEventPublisher;
import kr.hhplus.be.server.domain.coupon.event.type.CouponIssueRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponKafkaProducer implements CouponEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final String TOPIC_COUPON_ISSUE = "coupon-issue";

	@Override
	public void publish(CouponIssueRequestEvent event) {
		try {
			log.info("쿠폰 발급 요청 이벤트 발행 시작: {}", event);

			// 순서 보장을 위해 couponId를 키로 사용
			// KafkaTemplate의 send 메서드를 사용하여 이벤트를 발행
			kafkaTemplate.send(
					TOPIC_COUPON_ISSUE,
					event.couponId().toString(),
					event)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("쿠폰 발급 요청 이벤트 발행 성공: {}, offset={}, partition={}",
							result,
							result.getRecordMetadata().offset(),
							result.getRecordMetadata().partition());
					} else {
						log.error("쿠폰 발급 요청 이벤트 발행 실패: {}", event, ex);
					}
				});
		} catch (Exception e) {
			log.error("쿠폰 발급 요청 이벤트 발행 중 예외 발생: {}", event, e);
			throw new RuntimeException("쿠폰 발급 요청 이벤트 전송 실패", e);
		}
	}
}
