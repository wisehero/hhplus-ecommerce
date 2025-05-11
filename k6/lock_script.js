import http from 'k6/http';
import {check} from 'k6';

export const options = {
    vus: 50,
    duration: '1s'
};

export default function () {
    // const url = 'http://localhost:8080/api/v1/coupons/issue/spin';
    const url = 'http://localhost:8080/api/v1/coupons/issue/pubsub';

    const payload = JSON.stringify({
        userId: __VU,
        couponId: 1
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status 204 or 409': (r) => r.status === 204 || r.status === 409,
    });
}