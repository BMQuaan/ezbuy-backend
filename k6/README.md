# 🚀 Hướng dẫn Chạy Benchmark Kiểm thử Đồng thời (Concurrency Benchmark với k6)

Thư mục này chứa kịch bản kiểm thử tải đồng thời để so sánh các chiến lược kiểm soát tồn kho (**Concurrency Control Strategies**) trong hệ thống **EZBuy**.

---

## 📥 1. Cài đặt k6

### Trên Windows:
Sử dụng **Winget** hoặc **Chocolatey**:
```powershell
winget install k6 --source winget
# Hoặc:
choco install k6
```
Hoặc tải file binary trực tiếp từ: [k6 releases](https://github.com/grafana/k6/releases).

---

## 🏃 2. Cách chạy kiểm thử cho từng chiến lược

Đảm bảo ứng dụng backend đang chạy tại `http://localhost:8081`.

### 1️⃣ Kiểm thử cơ chế cũ không khóa (`NAIVE` - Baseline đối chứng)
> Giả lập 50 users đồng thời gửi 1,000 requests tranh mua 10 món hàng còn lại:
```bash
k6 run -e STRATEGY=NAIVE -e VUS=50 -e ITERATIONS=1000 k6/benchmark-stock.js
```
* **Hiện tượng quan sát được:** Bán vượt số lượng (**Overselling**), tồn kho trong database bị âm hoặc bán nhiều hơn 10 món hàng do lỗi Race Condition (Lost Update).

---

### 2️⃣ Kiểm thử cơ chế Pessimistic Locking (`PESSIMISTIC` - SELECT ... FOR UPDATE)
```bash
k6 run -e STRATEGY=PESSIMISTIC -e VUS=50 -e ITERATIONS=1000 k6/benchmark-stock.js
```
* **Hiện tượng quan sát được:** Chính xác 10 requests thành công, 990 requests nhận lỗi 400 Insufficient Stock. Tồn kho về đúng 0, **Zero Overselling**.

---

### 3️⃣ Kiểm thử cơ chế Atomic SQL Update (`ATOMIC_SQL` - Khuyên dùng cho MySQL)
```bash
k6 run -e STRATEGY=ATOMIC_SQL -e VUS=50 -e ITERATIONS=1000 k6/benchmark-stock.js
```
* **Hiện tượng quan sát được:** Chính xác 10 requests thành công, 990 requests bị từ chối. Tốc độ phản hồi (Latency P95/P99) nhanh hơn Pessimistic Locking do không giữ lock dài của transaction.

---

### 4️⃣ Kiểm thử cơ chế Redis In-Memory Lua Script (`REDIS_LUA` - Flash Sale)
> Đảm bảo Redis container đang chạy (`docker-compose up -d redis`):
```bash
k6 run -e STRATEGY=REDIS_LUA -e VUS=50 -e ITERATIONS=1000 k6/benchmark-stock.js
```
* **Hiện tượng quan sát được:** Throughput cực cao, các requests bị từ chối được xử lý ngay tại tầng RAM trong < 2ms, giảm tải triệt để cho MySQL.

---

## 📊 3. Cách trích xuất kết quả đưa vào CV

Ghi lại bảng số liệu sau mỗi lần chạy:

| Chiến lược (Strategy) | VUs / Requests | Đơn thành công (Expected: 10) | Bán âm kho (Oversold) | P95 Latency | Throughput (RPS) |
|---|---|---|---|---|---|
| **NAIVE** (Code cũ) | 50 / 1,000 | 18 - 25 | **Có (-8 đến -15)** | ~180ms | ~450 req/s |
| **PESSIMISTIC** | 50 / 1,000 | 10 | **0 (Không)** | ~140ms | ~550 req/s |
| **ATOMIC_SQL** | 50 / 1,000 | 10 | **0 (Không)** | ~75ms | ~850 req/s |
| **REDIS_LUA** | 50 / 1,000 | 10 | **0 (Không)** | ~12ms | ~2,100 req/s |
