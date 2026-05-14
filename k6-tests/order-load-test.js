import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
  scenarios: {
    order_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 500 },
        { duration: '2m', target: 500 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8084';

export default function () {
  // Test health
  const healthRes = http.get(`${BASE_URL}/orders/health`);
  check(healthRes, {
    'orders health UP': (r) => r.status === 200,
  });

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'k6-tests/order-results.json': JSON.stringify(data),
  };
}