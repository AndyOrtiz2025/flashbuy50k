import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// Métricas personalizadas
const successfulReservations = new Counter('successful_reservations');
const failedReservations = new Counter('failed_reservations');
const errorRate = new Rate('error_rate');

export const options = {
  scenarios: {
    // Escenario 1: Carga gradual
    ramp_up: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },   // Subir a 100 usuarios en 30s
        { duration: '1m', target: 500 },    // Subir a 500 usuarios en 1 min
        { duration: '2m', target: 1000 },   // Mantener 1000 usuarios por 2 min
        { duration: '30s', target: 0 },     // Bajar a 0
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% de requests bajo 2 segundos
    http_req_failed: ['rate<0.1'],      // Menos del 10% de errores
    error_rate: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8083';
const PRODUCT_ID = '11111111-1111-1111-1111-111111111111';

export default function () {
  // Test 1: Health check
  const healthRes = http.get(`${BASE_URL}/inventory/health`);
  check(healthRes, {
    'health status 200': (r) => r.status === 200,
    'health UP': (r) => r.json('status') === 'UP',
  });

  // Test 2: Consultar stock
  const stockRes = http.get(`${BASE_URL}/inventory/${PRODUCT_ID}`);
  check(stockRes, {
    'stock status 200 or 404': (r) => r.status === 200 || r.status === 404,
  });

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'k6-tests/inventory-results.json': JSON.stringify(data),
  };
}