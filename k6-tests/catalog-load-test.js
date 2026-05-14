import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    catalog_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '1m', target: 1000 },
        { duration: '2m', target: 1000 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = 'http://localhost:8082';

export default function () {
  // Test health
  const healthRes = http.get(`${BASE_URL}/catalog/health`);
  check(healthRes, {
    'catalog health UP': (r) => r.status === 200,
  });

  // Test temporadas activas
  const seasonsRes = http.get(`${BASE_URL}/catalog/seasons/active`);
  check(seasonsRes, {
    'seasons status ok': (r) => r.status === 200,
  });

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'k6-tests/catalog-results.json': JSON.stringify(data),
  };
}