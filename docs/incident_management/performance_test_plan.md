# 항해 플러스 이커머스 프로젝트 성능 테스트 계획서

## 테스트 목표 및 환경

### 테스트 대상 및 목표

- 선착순 쿠폰 발급 API와 인기 상품 조회 API의 성능을 테스트 한다.
- 각 API 응답 시간 및 처리량 성능 한계점 파악
- 시스템 병목 지점 식별 및 개선점 도출

### 테스트 환경

- 애플리케이션 : Spring Boot 3.4.1, Open JDK 17
    - 최대 스레드 풀 : 기본값 200
    - 커넥션 풀 : 최소 20, 최대 100
- 데이터베이스: MySQL 8.0
- 리소스 제한 : CPU 4.0 cores, Memory 8GB
- 네트워크 : 로컬 Docker-Compose

## 테스트 계획

### 선착순 쿠폰 발급 API - 스파이크 테스트

선착순 쿠폰 발급 API는 현재 WAS - DB 구조로 트랜잭션을 수행하고 있으며 동시성 제어를 위해 비관적 락이 적용되어 있다.  
따라서 아래와 같은 스파이크 테스트를 통해 응답 시간의 한계점을 파악한다.

- 급격한 부하를 vUser 300, 400, 500으로 증가시키면서 대부분의 응답 시간이 1초 이내로 유지되는지 확인

```javascript
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
        {duration: '5s', target: 300},     // 급격한 스파이크
        {duration: '1m', target: 300},     // 고부하 유지
        {duration: '10s', target: 100},    // 점진적 감소
        {duration: '10s', target: 0},      // 종료
    ],

    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],

    // 스파이크 테스트용 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<1000'],      // 95%가 1초 미만
        http_req_failed: ['rate<0.1'],          // 실패율 10% 미만
        coupon_issue_success: ['count>=8000'],   // 최소 8000개 발급 성공
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
        '응답시간_허용범위': (r) => r.timings.duration < 2000, // 2초 이내
        '연결_성공': (r) => r.status !== 0,
    });

    // 스파이크 상황에서는 sleep 최소화
    if (res.status === 429) {
        sleep(0.1); // Rate limiting 시 짧은 대기
    }
}
```

### 인기 상품 조회 API - 스트레스 테스트

인기 상품 조회는 싱글 레디스에 캐싱을 적용하였으며 점진적 부하 증가로 시스템의 한계점을 파악한다.  
현재 일간, 주간, 월간 베스트셀러 조회 기능이 있으나, 이 가운데 월간을 선택한다. 실제 사용자 행동 패턴 상  
월간 베스트셀러 상품은 정말 많이 팔리는 검증된 상품이라는 특징 때문에 사용자들이 가장 많이 찾는 베스트셀러 메뉴이기 때문이다.

- 부하 증가 : 100 -> 200 -> 400 -> 600 -> 1000 RPS
- 데이터셋 : 약 100만 건의 베스트셀러 데이터

### 인기 상품 조회 API - K6 스크립트

```javascript
import http from 'k6/http';
import {check} from 'k6';

export const options = {
    stages: [
        {duration: '30s', target: 50},     // 워밍업
        {duration: '1m', target: 100},
        {duration: '1m', target: 300},
        {duration: '1m', target: 500},
        {duration: '30s', target: 0},      // 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<300', 'p(99)<500'], // SLA: 95% 응답 300ms, 99%는 0.5초 이내
        http_req_failed: ['rate<0.01'],                 // 실패율 1% 미만
    },
};

export default function () {
    const url = 'http://localhost:8080/api/v1/bestsellers?period=MONTHLY'; // 실제 서비스 엔드포인트로 대체
    const res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 1.5s': (r) => r.timings.duration < 1500,
    });
}
```





