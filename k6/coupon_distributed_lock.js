import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// Pub/Sub 방식 테스트 구성
export const options = {
    scenarios: {
        pubsub_test: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: 500,
            maxVUs: 1000,
            stages: [
                { duration: '10s', target: 100 }, // 10초 동안 점진적으로 초당 100 요청까지 증가
                { duration: '30s', target: 100 }, // 30초 동안 초당 100 요청 유지
                { duration: '5s', target: 0 },    // 5초 동안 점진적으로 트래픽 감소
            ],
            exec: 'pubsubIssue',
        }
    }
};

// 사용자 ID 준비 (10000명의 가상 사용자)
const users = new SharedArray('users', function() {
    const arr = [];
    for (let i = 1; i <= 10000; i++) {
        arr.push(i);
    }
    return arr;
});

// 테스트할 쿠폰 ID
const COUPON_ID = 2; // 테스트할 실제 쿠폰 ID로 변경 필요

// Pub/Sub 방식 테스트 함수
export function pubsubIssue() {
    // 사용자 ID 랜덤 선택
    const userId = users[Math.floor(Math.random() * users.length)];

    const url = 'http://localhost:8080/api/v1/coupons/issue/pubsub';
    const payload = JSON.stringify({
        userId: userId,
        couponId: COUPON_ID
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(url, payload, params);

    check(response, {
        'status is 204': (r) => r.status === 204,
        'status is 400 or 409 (예상된 오류)': (r) => r.status === 400 || r.status === 409,
    });

    sleep(0.1);
}