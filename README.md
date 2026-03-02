# 🛍️ EZBuy - Backend API cho Nền tảng Thương mại Điện tử

---

## I. 📖 Giới thiệu

**EZBuy** là dự án **backend** cho một nền tảng **thương mại điện tử hiện đại**, được xây dựng bằng **Spring Boot**.  
Dự án cung cấp bộ **API RESTful** hoàn chỉnh để quản lý:

- Người dùng  
- Sản phẩm  
- Danh mục  
- Giỏ hàng  
- Đơn hàng  
- ...

Điểm đặc biệt của hệ thống là kiến trúc **microservice**, gồm:

- 🧩 **Service chính (Spring Boot)**: xử lý logic nghiệp vụ thương mại điện tử.  
- 🧠 **Service AI (FastAPI - Python)**: đảm nhiệm các tác vụ **AI / Machine Learning** (như **tìm kiếm sản phẩm bằng hình ảnh**).

---

## II. ⚙️ Các tính năng chính

### 1️⃣ Xác thực & Bảo mật
- Đăng ký, đăng nhập, quên mật khẩu (qua OTP email).  
- Bảo mật với **JWT** (Access Token & Refresh Token) lưu trong **HttpOnly Cookie**.  
- Tích hợp **Spring Security 6**.

### 2️⃣ Phân quyền người dùng (RBAC)
- **CUSTOMER**: khách hàng thông thường.  
- **ADMIN**: quản trị viên.  
→ Hệ thống kiểm soát quyền truy cập chi tiết theo vai trò.

### 3️⃣ Quản lý Người dùng
- Cập nhật thông tin cá nhân, đổi mật khẩu.  
- Tải lên ảnh đại diện (tích hợp **Cloudinary**).  
- Xác thực tài khoản qua OTP.

### 4️⃣ Quản lý Sản phẩm & Danh mục
- CRUD đầy đủ cho **sản phẩm** và **danh mục**.  
- **Xóa mềm** (`@SQLRestriction("is_active = true")`) để không mất dữ liệu.  
- Upload/xóa ảnh sản phẩm với **Cloudinary API**.  
- Tự động sinh **slug** từ tên sản phẩm bằng thư viện **Slugify**.

### 5️⃣ Quản lý Giỏ hàng
- Thêm, sửa, xóa sản phẩm trong giỏ hàng.  
- Tự động xóa sản phẩm khỏi giỏ nếu sản phẩm bị xóa mềm hoặc hết hàng.

### 6️⃣ Quản lý Đơn hàng
- Thanh toán an toàn với **Pessimistic Locking** → tránh tình trạng “mua trùng hàng”.  
- Áp dụng **mã giảm giá (promo code)** theo %.  
- Quản lý **trạng thái đơn hàng**:
  - ADMIN có thể duyệt đơn.
  - CUSTOMER có thể hủy đơn.  
- Tự động **hoàn trả tồn kho** khi đơn bị hủy.

### 7️⃣ Tìm kiếm bằng AI (Visual Search)
- Endpoint nhận **ảnh tải lên** từ người dùng.  
- Gửi ảnh đến **service AI (FastAPI)** để **dự đoán danh mục**.  
- Trả về danh sách gợi ý gồm:
  - **Top sản phẩm bán chạy nhất** trong danh mục.  
  - **Top sản phẩm mới nhất** trong danh mục.  
- Nếu AI không hoạt động hoặc không có dữ liệu → fallback sang:
  - **Top sản phẩm bán chạy và mới nhất toàn hệ thống**.

### 8️⃣ Kiến trúc & Kỹ thuật
- Sử dụng **DTOs + MapStruct** để ánh xạ dữ liệu.  
- **JPA Specifications** cho tìm kiếm linh hoạt.  
- **GlobalExceptionHandler** cho response lỗi đồng nhất.  
- **Event Listener** tự động dọn giỏ hàng khi sản phẩm bị vô hiệu hóa.

---

## III. 🧰 Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ |
|-------------|------------|
| **Ngôn ngữ chính** | Java 21 |
| **Framework Backend** | Spring Boot 3.x |
| **Bảo mật** | Spring Security 6 (JWT) |
| **ORM** | Spring Data JPA + Specifications |
| **Gửi email** | Spring Mail + Thymeleaf |
| **Mapper** | MapStruct |
| **Thư viện tiện ích** | Lombok, Slugify |
| **CSDL chính** | MySQL 8 |
| **Lưu trữ hình ảnh** | Cloudinary |
| **Service AI** | Python (FastAPI + TensorFlow/Keras hoặc PyTorch) |

---

## IV. 🏗️ Cấu trúc dự án

```
com.ezbuy.ezbuy
│
├── config/           # Cấu hình (SecurityConfig, CorsConfig, CloudinaryConfig,...)
├── constants/        # Các hằng số toàn cục
├── controllers/      # REST Controllers (API Endpoints)
├── dtos/             # Data Transfer Objects (Request/Response)
│   ├── request/
│   └── response/
├── entities/         # Thực thể JPA (User, Product, Category, Order,...)
├── enums/            # Các Enum (OrderStatus, Role, LoginType,...)
├── events/           # Event & Listener (ProductDeactivatedEvent, CartCleanupListener)
├── exceptions/       # Xử lý ngoại lệ (GlobalExceptionHandler, CustomException,...)
├── filters/          # JWT Authentication Filter
├── mappers/          # Ánh xạ DTO <-> Entity (MapStruct)
├── repositories/     # JPA Repository + Specifications
│   └── specifications/
└── services/         # Business Logic
    └── impl/         # Triển khai cụ thể của các service
```

---

## V. ⚡ Hướng dẫn cài đặt và chạy

---

### 1️⃣ Cài đặt Database (MySQL)
1. Đảm bảo **MySQL Server** đang chạy (mặc định: `port 3306`).  
2. Tạo cơ sở dữ liệu mới:
   ```sql
   CREATE DATABASE ezbuy;
   ```
3. Import dữ liệu từ file SQL dump (ezbuy_database.sql)

---

### 2️⃣ Cấu hình Spring Boot
Mở file `src/main/resources/application.properties`  
(hoặc tạo file `application-local.properties`) và cập nhật thông tin:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ezbuy
spring.datasource.username=root
spring.datasource.password=your_password

ai.classification.service.url=http://localhost:8000/predict

cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

spring.mail.username=your_email@example.com
spring.mail.password=your_email_password

app.jwt.secret=your_jwt_secret_key
```

---

### 3️⃣ Chạy ứng dụng Spring Boot
Có thể chạy bằng Maven hoặc IDE:

#### Cách 1: Maven CLI
```bash
mvn spring-boot:run
```

#### Cách 2: IntelliJ / VS Code
Chọn file chính → **Run as Spring Boot App**

---

## VI. 🧪 Kiểm thử API

Sau khi khởi động thành công:

| Thành phần | URL |
|-------------|------|
| **API Root** | `http://localhost:80818081/api` |

### Ví dụ Endpoint:
- Đăng ký tài khoản: `POST /api/v1/auth/register`  
- Đăng nhập: `POST /api/v1/auth/login`  
- Tìm kiếm bằng ảnh: `POST /api/v1/visual-search`  

---

## VII. 🔮 Hướng phát triển tương lai

✅ Tích hợp **Swagger (SpringDoc OpenAPI)** để sinh tài liệu API tự động.  
✅ Thêm **Docker Compose** để chạy toàn bộ hệ thống (Spring Boot + FastAPI + MySQL).  
✅ Dùng **Redis Cache** để cache kết quả AI và sản phẩm hot.  
✅ Giám sát hệ thống với **Spring Boot Actuator + Prometheus + Grafana**.  
✅ Chuyển sang giao tiếp **asynchronous** giữa service Java và Python (Kafka / RabbitMQ).

---

## ❤️ Tác giả
Dự án được phát triển bởi nhóm phát triển độc lập của **EZBuy**.  
Nếu bạn muốn hợp tác hoặc đóng góp, hãy liên hệ qua email trong cấu hình dự án.

---

> 💡 **EZBuy** là nền tảng thương mại điện tử hiện đại, an toàn và thông minh,  
> kết hợp giữa **Spring Boot** & **AI FastAPI** để mang đến trải nghiệm mua sắm tối ưu nhất.
