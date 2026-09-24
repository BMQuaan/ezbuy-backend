# 🛍️ EZBuy - Backend API cho Nền tảng Thương mại Điện tử Chịu tải cao

---

## I. 📖 Giới thiệu

**EZBuy** là hệ thống **Backend RESTful API** cho nền tảng thương mại điện tử hiện đại, được xây dựng trên nền **Spring Boot 3** và **Java 21**, tích hợp kiến trúc xử lý đồng thời (**High Concurrency Control**), bộ nhớ đệm phân tán **Redis** và dịch vụ trí tuệ nhân tạo **AI Microservice (FastAPI - Python)** hỗ trợ tìm kiếm sản phẩm bằng hình ảnh (Visual Search).

---

## II. ⚙️ Các tính năng cốt lõi

### 1️⃣ Xác thực & Bảo mật (Authentication & RBAC)
* Đăng ký, đăng nhập, quên mật khẩu (xác thực OTP qua Email).
* Bảo mật với **JWT** (Access Token & Refresh Token) lưu trữ an toàn trong **HttpOnly Cookie**, phòng chống tấn công XSS/CSRF.
* Phân quyền chi tiết theo vai trò (**RBAC**): `CUSTOMER` và `ADMIN`.

### 2️⃣ Quản lý Sản phẩm & Danh mục (Catalog Management)
* CRUD sản phẩm, danh mục, thương hiệu đầy đủ.
* Cơ chế **xóa mềm (Soft Delete)** thông minh qua Hibernate `@SQLRestriction("is_active = true")`.
* Quản lý upload media đa phương tiện tích hợp **Cloudinary API**.
* Tự động sinh đường dẫn thân thiện SEO (slug) với thư viện **Slugify**.

### 3️⃣ Kiểm soát Đồng thời & Chống Bán âm kho (High-Concurrency Inventory Control)
* Triển khai kiến trúc **Strategy Pattern** cho phép cắm rút và chuyển đổi linh hoạt các cơ chế trừ tồn kho qua HTTP Header `X-Stock-Strategy`:
  * **`NAIVE`**: Cơ chế ban đầu (Read-Modify-Write trên RAM không lock) phục vụ làm đối chứng thực nghiệm cho benchmark.
  * **`PESSIMISTIC`**: Khóa bi quan `SELECT ... FOR UPDATE` ở tầng Database với cơ chế **tự động sắp xếp ID sản phẩm** nhằm ngăn chặn triệt để hiện tượng Deadlock giữa các transaction giỏ hàng đa mặt hàng.
  * **`ATOMIC_SQL`**: Câu lệnh SQL nguyên tử `UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?` tận dụng trực tiếp Row-level lock của InnoDB, giải phóng kết nối nhanh chóng, an toàn tuyệt đối cho các đơn hàng thông thường.
  * **`REDIS_LUA`**: Pre-decrement trên RAM của Redis bằng **Lua Script nguyên tử** (`DECRBY`) cho các sự kiện **Flash Sale** lưu lượng lớn; chặn đứng hàng ngàn request hết hàng trong < 1ms, giảm tải 99% áp lực truy vấn cho MySQL.

### 4️⃣ Thanh toán Trực tuyến VNPay (Secure Payment Gateway)
* Tích hợp cổng thanh toán **VNPay** với cơ chế bảo mật chữ ký số **SHA512 Checksum**.
* Tách biệt rõ ràng 2 luồng:
  * `/vnpay-callback`: Trả kết quả hiển thị trên giao diện người dùng.
  * `/vnpay-ipn`: Server-to-Server Webhook chuẩn hóa với tính chất **Idempotency** (chống xử lý trùng lặp giao dịch) và kiểm tra đối soát số tiền thực trả.

### 5️⃣ Tìm kiếm Bằng Trí tuệ Nhân tạo (AI Visual Search)
* Tiếp nhận ảnh tải lên từ người dùng, gọi vi dịch vụ **FastAPI (Python)** phân tích đặc trưng ảnh và dự đoán danh mục sản phẩm.
* Cơ chế tự động Fallback linh hoạt: Nếu service AI quá tải hoặc không có dữ liệu, hệ thống tự động fallback sang gợi ý sản phẩm bán chạy nhất hệ thống.

---

## III. 🧰 Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ / Phiên bản |
|---|---|
| **Ngôn ngữ chính** | Java 21 (LTS) |
| **Framework Backend** | Spring Boot 3.5.6 |
| **Bảo mật** | Spring Security 6, JJWT 0.13 |
| **Cơ sở dữ liệu** | MySQL 8.0 (InnoDB) |
| **Bộ nhớ đệm & Khóa phân tán** | Redis 7 (Alpine) + Spring Data Redis |
| **Kiểm thử hiệu năng (Stress Test)** | Grafana k6, JUnit 5, Mockito |
| **Object Mapping** | MapStruct 1.6, Lombok |
| **Lưu trữ hình ảnh** | Cloudinary API |
| **Gửi email thông báo** | Spring Mail + Thymeleaf Engine |
| **Dịch vụ AI Microservice** | Python 3 + FastAPI + PyTorch/TensorFlow |
| **Frontend Client** | Next.js 15 + TypeScript + TailwindCSS |
| **Đóng gói & Triển khai** | Docker, Docker Compose |

---

## IV. 🏗️ Cấu trúc dự án Backend

```
com.ezbuy.ezbuy
│
├── config/              # Cấu hình hệ thống (Security, Redis, Cors, Cloudinary,...)
├── constants/           # Hằng số, danh sách Security Whitelist
├── controllers/         # REST API Controllers (Order, Product, Benchmark, Payment,...)
├── dtos/                # Data Transfer Objects (Request / Response)
├── entities/            # JPA Entities (Product, Order, User, PaymentTransaction,...)
├── enums/               # Enums (StockStrategyType, OrderStatus, PaymentStatus,...)
├── events/              # Event-Driven nội bộ (ProductDeactivatedEvent, CartCleanupListener)
├── exceptions/          # Xử lý ngoại lệ tập trung (GlobalExceptionHandler, InsufficientStockException)
├── filter/              # Bộ lọc JWT Authentication Filter
├── mappers/             # Ánh xạ DTO <-> Entity (MapStruct)
├── repositories/        # Spring Data JPA Repositories & Specifications
├── services/            # Business Logic Services & Implementations
└── strategies/          # Strategy Pattern kiến trúc kiểm soát đồng thời
    └── stock/
        ├── StockDeductionStrategy.java       # Interface chung
        ├── StockDeductionContext.java        # Bộ điều phối chiến lược động
        └── impl/
            ├── NaiveStockStrategy.java       # Chiến lược cũ (Baseline)
            ├── PessimisticStockStrategy.java # Khóa bi quan DB
            ├── AtomicSqlStockStrategy.java   # Cập nhật SQL nguyên tử
            └── RedisLuaStockStrategy.java    # Khóa RAM Redis Flash Sale
```

---

## V. ⚡ Hướng dẫn cài đặt & Khởi động

### 1️⃣ Chuẩn bị biến môi trường
Tạo file `.env` tại thư mục gốc backend (dựa trên mẫu `.env.example`):
```bash
cp .env.example .env
```
Cấu hình các thông số cần thiết (mật khẩu DB, JWT Secret, Redis, Cloudinary, VNPay).

---

### 2️⃣ Cách 1: Khởi động 1-Click bằng Docker Compose (Khuyên dùng)
Toàn bộ hệ sinh thái (MySQL, Redis, Spring Boot Backend) được đóng gói sẵn trong `docker-compose.yml`:

```bash
# Khởi động toàn bộ các service
docker compose up -d

# Xem log thời gian thực của backend
docker compose logs -f spring-app
```
* **API Backend:** `http://localhost:8081`
* **MySQL Database:** `localhost:3307`
* **Redis Service:** `localhost:6379`

---

### 3️⃣ Cách 2: Khởi động Local từng phần (Cho môi trường phát triển)
1. Khởi động hạ tầng Database và Redis:
   ```bash
   docker compose up -d db redis
   ```
2. Chạy ứng dụng Spring Boot bằng Maven Wrapper:
   ```bash
   # Trên Windows:
   .\mvnw.cmd spring-boot:run

   # Trên Linux/macOS:
   ./mvnw spring-boot:run
   ```

---

## 📊 VI. Kiểm thử Chịu tải & Benchmark với k6

Hệ thống đi kèm kịch bản benchmark chuẩn **k6** tại thư mục [k6/benchmark-stock.js](k6/benchmark-stock.js).  
Kịch bản giả lập **50 Virtual Users đồng thời gửi 1,000 requests** tranh mua **10 sản phẩm cuối cùng** trong kho:

```bash
# 1. Đo đạc cơ chế cũ (đối chứng lỗi bán âm kho):
k6 run -e STRATEGY=NAIVE k6/benchmark-stock.js

# 2. Đo đạc cơ chế Atomic SQL (Zero Overselling):
k6 run -e STRATEGY=ATOMIC_SQL k6/benchmark-stock.js

# 3. Đo đạc cơ chế Pessimistic Locking:
k6 run -e STRATEGY=PESSIMISTIC k6/benchmark-stock.js

# 4. Đo đạc cơ chế Flash Sale (Redis Lua Script - Tốc độ cao nhất):
k6 run -e STRATEGY=REDIS_LUA k6/benchmark-stock.js
```

### 📈 Bảng đối sánh số liệu thực nghiệm:

| Chiến lược (Strategy) | Số đơn bán (Kho = 10) | Bán vượt (Overselling) | P95 Latency | Throughput (RPS) |
|---|:---:|:---:|:---:|:---:|
| **NAIVE** (Cũ) | ❌ **92 đơn** | ❌ **Bán lố 82 đơn** | 36.5ms | ~2,719 req/s |
| **PESSIMISTIC** | ✅ **10 đơn** | ✅ **0 (Zero)** | 47.1ms | ~1,968 req/s |
| **ATOMIC_SQL** | ✅ **10 đơn** | ✅ **0 (Zero)** | 61.0ms | ~1,176 req/s |
| **REDIS_LUA** (Flash Sale) | ✅ **10 đơn** | ✅ **0 (Zero)** | 🚀 **29.3ms** | 🚀 **~2,250 req/s** |

---

## 🧪 VII. Chạy Kiểm thử Tự động (Automated Tests)

Chạy toàn bộ bộ test tích hợp và kiểm thử đa luồng (`ConcurrentStockDeductionTest`):
```bash
.\mvnw.cmd test
```

---

## 🔮 VIII. Lộ trình phát triển tiếp theo (Roadmap)
- [x] **Phase 1:** Concurrency Control với Strategy Pattern (Zero Overselling) & k6 Benchmark.
- [ ] **Phase 2:** Caching Đa tầng với Redis (Cache-Aside, TTL Jitter chống Avalanche, chống Cache Stampede).
- [ ] **Phase 3:** Event-Driven Architecture với Message Queue (RabbitMQ / Kafka) & Transactional Outbox Pattern.
- [ ] **Phase 4:** Observability (Spring Boot Actuator + Prometheus + Grafana Dashboard + Distributed Tracing).
