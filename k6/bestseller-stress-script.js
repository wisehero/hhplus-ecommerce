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
        http_req_duration: ['p(95)<500', 'p(99)<1500'], // SLA: 95% 응답 500ms, 99%는 1.5초 이내
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