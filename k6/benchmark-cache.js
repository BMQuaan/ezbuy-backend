import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Custom Metrics
const successfulRequests = new Counter('successful_requests');
const failedRequests = new Counter('failed_requests');
const cacheReadDuration = new Trend('cache_read_duration');

// Configurable via CLI:
// e.g. "/c/Program Files/k6/k6.exe" run -e TARGET=CATEGORY_TREE -e VUS=50 -e DURATION=15s k6/benchmark-cache.js
// or "/c/Program Files/k6/k6.exe" run -e TARGET=PRODUCT_DETAIL -e PRODUCT_ID=2 -e VUS=100 -e ITERATIONS=2000 k6/benchmark-cache.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const TARGET = __ENV.TARGET || 'CATEGORY_TREE'; // CATEGORY_TREE | PRODUCT_DETAIL | MIXED
const PRODUCT_ID = __ENV.PRODUCT_ID || '2';
const VUS = parseInt(__ENV.VUS || '50');
const ITERATIONS = __ENV.ITERATIONS ? parseInt(__ENV.ITERATIONS) : null;
const DURATION = __ENV.DURATION || '15s';

export const options = ITERATIONS
    ? {
        scenarios: {
            cache_read_iterations: {
                executor: 'shared-iterations',
                vus: VUS,
                iterations: ITERATIONS,
                maxDuration: '30s',
            },
        },
        thresholds: {
            failed_requests: ['count<1'], // Zero errors
            http_req_duration: ['p(95)<20'], // With Redis cache, 95% of requests should be < 20ms
        },
    }
    : {
        scenarios: {
            cache_read_stress: {
                executor: 'constant-vus',
                vus: VUS,
                duration: DURATION,
            },
        },
        thresholds: {
            failed_requests: ['count<1'], // Zero errors
            http_req_duration: ['p(95)<20'], // With Redis cache, 95% of requests should be < 20ms
        },
    };

export function setup() {
    console.log(`===========================================================`);
    console.log(`🚀 BẮT ĐẦU BENCHMARK HIỆU NĂNG ĐỌC REDIS CACHING (PHASE 2)`);
    console.log(`👉 Mục tiêu kiểm thử (TARGET): ${TARGET}`);
    console.log(`👉 Virtual Users (VUs): ${VUS}`);
    if (ITERATIONS) {
        console.log(`👉 Tổng số requests (Iterations): ${ITERATIONS}`);
    } else {
        console.log(`👉 Thời gian chạy (Duration): ${DURATION}`);
    }
    console.log(`👉 Base URL: ${BASE_URL}`);
    console.log(`===========================================================`);

    // Warm up the cache with 1 initial request (Cold Start -> Cache Populated)
    const warmupUrl = TARGET === 'PRODUCT_DETAIL'
        ? `${BASE_URL}/api/products/${PRODUCT_ID}`
        : `${BASE_URL}/api/categories/tree`;

    const warmRes = http.get(warmupUrl);
    if (warmRes.status === 200) {
        console.log(`🔥 Warmup thành công! Dữ liệu đã được nạp vào Redis Cache.`);
    } else {
        console.warn(`⚠️ Warmup status: ${warmRes.status}. Server có thể chưa sẵn sàng.`);
    }

    return { warmedUp: warmRes.status === 200 };
}

export default function () {
    let url;
    if (TARGET === 'CATEGORY_TREE') {
        url = `${BASE_URL}/api/categories/tree`;
    } else if (TARGET === 'PRODUCT_DETAIL') {
        url = `${BASE_URL}/api/products/${PRODUCT_ID}`;
    } else {
        // MIXED: 50% category tree, 50% product detail
        url = Math.random() < 0.5
            ? `${BASE_URL}/api/categories/tree`
            : `${BASE_URL}/api/products/${PRODUCT_ID}`;
    }

    const res = http.get(url, {
        headers: {
            'Accept': 'application/json',
        },
    });

    const isSuccess = check(res, {
        'status is 200': (r) => r.status === 200,
        'has response body': (r) => r.body && r.body.length > 0,
    });

    if (isSuccess) {
        successfulRequests.add(1);
        cacheReadDuration.add(res.timings.duration);
    } else {
        failedRequests.add(1);
    }
}

export function teardown() {
    console.log(`\n===========================================================`);
    console.log(`🏁 HOÀN TẤT BENCHMARK REDIS CACHING!`);
    console.log(`Kiểm tra báo cáo chi tiết phía dưới để lấy thông số RPS và P95 Latency.`);
    console.log(`===========================================================`);
}
