import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter} from 'k6/metrics';

// 커스텀 메트릭
const couponIssueSuccess = new Counter('coupon_issue_success');
const couponAlreadyIssued = new Counter('coupon_already_issued');
const systemErrors = new Counter('system_errors');

export const options = {
    stages: [
        {duration: '5s', target: 20},      // 워밍업
        {duration: '5s', target: 1000},     // 급격한 스파이크
        {duration: '1m', target: 1000},     // 고부하 유지
        {duration: '10s', target: 100},    // 점진적 감소
        {duration: '10s', target: 0},      // 종료
    ],

    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    // 스파이크 테스트용 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<5000'],      // 95%가 5초 미만
        http_req_failed: ['rate<0.1'],          // 실패율 10% 미만
        coupon_issue_success: ['count>=9000'],   // 최소 8000개 발급 성공
    },
};

const BASE_URL = 'http://localhost:8080';
const COUPON_ID = 13;

export default function () {
    const userId = Math.floor(Math.random() * 50000) + 1; // 사용자 풀 증가

    const payload = JSON.stringify({
        userId: userId,
        couponId: COUPON_ID
    });

    const res = http.post(`${BASE_URL}/api/v1/coupons/issue`, payload, {
        headers: {'Content-Type': 'application/json'},
        timeout: '10s', // 스파이크 상황에서 타임아웃 증가
        // 409를 정상으로 처리
        responseCallback: http.expectedStatuses(200, 204, 409)
    });

    // 메트릭 수집
    if (res.status === 204) {
        couponIssueSuccess.add(1);
    } else if (res.status === 409) {
        couponAlreadyIssued.add(1);
    } else if (res.status >= 500 || res.status === 0) {
        systemErrors.add(1);
    }

    // 스파이크 테스트 체크
    check(res, {
        '시스템_생존': (r) => [204, 409].includes(r.status),
        '응답시간_허용범위': (r) => r.timings.duration < 10000, // 10초 이내
        '연결_성공': (r) => r.status !== 0,
    });

    // 스파이크 상황에서는 sleep 최소화
    if (res.status === 429) {
        sleep(0.1); // Rate limiting 시 짧은 대기
    }
}