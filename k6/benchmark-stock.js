import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Custom Metrics
const successfulDeductions = new Counter('successful_deductions');
const outOfStockResponses = new Counter('out_of_stock_responses');
const unexpectedErrors = new Counter('unexpected_errors');
const stockDeductionTrend = new Trend('stock_deduction_duration');

// Configurable via CLI: e.g. k6 run -e STRATEGY=ATOMIC_SQL -e VUS=100 -e ITERATIONS=1000 benchmark-stock.js
const STRATEGY = __ENV.STRATEGY || 'ATOMIC_SQL'; // NAIVE | PESSIMISTIC | ATOMIC_SQL | REDIS_LUA
const PRODUCT_ID = __ENV.PRODUCT_ID || '2';
const QUANTITY = __ENV.QUANTITY || '1';
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export const options = {
    scenarios: {
        flash_sale_simulation: {
            executor: 'shared-iterations',
            vus: parseInt(__ENV.VUS || '50'),
            iterations: parseInt(__ENV.ITERATIONS || '1000'),
            maxDuration: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'], // less than 1% 5xx errors
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    },
};

export function setup() {
    console.log(`===========================================================`);
    console.log(`🚀 BẮT ĐẦU BENCHMARK KIỂM TRA TRANH CHẤP TỒN KHO (FLASH SALE)`);
    console.log(`👉 Chiến lược kiểm thử: ${STRATEGY}`);
    console.log(`👉 Sản phẩm ID: ${PRODUCT_ID} | Số lượng mỗi request: ${QUANTITY}`);
    console.log(`👉 Virtual Users (VUs): ${options.scenarios.flash_sale_simulation.vus}`);
    console.log(`👉 Tổng số requests (Iterations): ${options.scenarios.flash_sale_simulation.iterations}`);
    console.log(`===========================================================`);

    // Reset stock before running the test (e.g. stock = 10)
    const resetRes = http.post(`${BASE_URL}/api/benchmark/reset-stock?productId=${PRODUCT_ID}&stock=10`);
    if (resetRes.status === 200) {
        console.log(`✅ Đã reset kho sản phẩm ${PRODUCT_ID} về: 10 món hàng`);
    } else {
        console.warn(`⚠️ Reset stock status: ${resetRes.status}. Continuing test...`);
    }

    return { initialStock: 10 };
}

export default function () {
    const url = `${BASE_URL}/api/benchmark/stock-deduction?productId=${PRODUCT_ID}&quantity=${QUANTITY}`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Stock-Strategy': STRATEGY,
        },
    };

    const res = http.post(url, null, params);
    stockDeductionTrend.add(res.timings.duration);

    if (res.status === 200) {
        successfulDeductions.add(1);
    } else if (res.status === 400) {
        outOfStockResponses.add(1);
    } else {
        unexpectedErrors.add(1);
    }

    check(res, {
        'status is 200 or 400 (Expected)': (r) => r.status === 200 || r.status === 400,
    });
}

export function teardown(data) {
    // Check final stock in DB after the test
    const stockRes = http.get(`${BASE_URL}/api/benchmark/stock/${PRODUCT_ID}`);
    let finalMysqlStock = 'N/A';
    let finalRedisStock = 'N/A';

    if (stockRes.status === 200) {
        const body = JSON.parse(stockRes.body);
        finalMysqlStock = body.data.mysqlStock;
        finalRedisStock = body.data.redisStock;
    }

    console.log(`\n===========================================================`);
    console.log(`📊 KẾT QUẢ TỔNG KẾT CHO CHIẾN LƯỢC: ${STRATEGY}`);
    console.log(`-----------------------------------------------------------`);
    console.log(`🔹 Tồn kho ban đầu:         ${data.initialStock}`);
    console.log(`🔹 Tồn kho cuối cùng (DB):   ${finalMysqlStock}`);
    console.log(`🔹 Tồn kho cuối cùng (Redis):${finalRedisStock}`);
    if (finalMysqlStock < 0) {
        console.log(`❌ BÁO ĐỘNG: Bị bán âm kho (Overselling) ${Math.abs(finalMysqlStock)} sản phẩm!`);
    } else if (finalMysqlStock === 0) {
        console.log(`✅ CHÍNH XÁC: Kho đã về 0, không bị bán âm, không bị lost update!`);
    }
    console.log(`===========================================================\n`);
}
