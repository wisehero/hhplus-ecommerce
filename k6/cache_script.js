import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    stages: [
        {duration: '30s', target: 10},   // 초기 워밍업
        {duration: '30s', target: 50},   // 서서히 증가
        {duration: '30s', target: 100},  // 중간 강도
        {duration: '30s', target: 200},  // 높은 부하
        {duration: '30s', target: 500},  // 최대 부하
        {duration: '30s', target: 0},    // 완화
    ],
};

const BASE_URL = 'http://localhost:8080/api/v1/bestsellers';
const PERIOD = 'MONTHLY';

export default function () {
    const res = http.get(`${BASE_URL}?period=${PERIOD}`);
    console.log(`[${PERIOD}] Status: ${res.status} - Duration: ${res.timings.duration}ms`);

    sleep(0.5);
}