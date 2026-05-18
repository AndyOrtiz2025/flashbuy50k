import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    flash_sale: {
      executor: 'constant-arrival-rate',
      rate: 1000,           // 1000 requests por segundo
      timeUnit: '1s',
      duration: '50s',      // Durante 50 segundos = ~50,000 requests total
      preAllocatedVUs: 500,
      maxVUs: 2000,
    },
  },
  thresholds: {
    http_req_duration: ['p(99)<3000'],  // 99% bajo 3 segundos
    http_req_failed: ['rate<0.2'],      // Menos del 20% de errores (esperado bajo alta carga)
  },
};

const BASE_URL = 'http://localhost:8083';
const PRODUCT_ID = '11111111-1111-1111-1111-111111111111';

export default function () {
  const res = http.get(`${BASE_URL}/inventory/${PRODUCT_ID}`);
  check(res, {
    'status ok': (r) => r.status === 200 || r.status === 404 || r.status === 429,
  });
}

export function handleSummary(data) {
  return {
    'k6-tests/stress-results.json': JSON.stringify(data),
  };
}
