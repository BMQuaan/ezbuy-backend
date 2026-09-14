-- =========================================================================
-- Migration Script: Cập nhật CSDL cho tính năng Thanh toán VNPay & Audit Log
-- Project: EZBuy
-- =========================================================================

-- 1. Thêm cột payment_status vào bảng orders
ALTER TABLE `orders` 
ADD COLUMN `payment_status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' AFTER `status`;

-- 2. Đồng bộ các đơn hàng VNPay đã từng thanh toán trước đây:
-- Đơn có giao dịch VNPay thành công và không bị hủy -> PAID
UPDATE `orders` 
SET `payment_status` = 'PAID' 
WHERE `vnp_transaction_no` IS NOT NULL 
  AND `status` <> 'CANCELLED';

-- Đơn có giao dịch VNPay nhưng sau đó bị hủy -> REFUNDED (đã/chờ hoàn tiền)
UPDATE `orders` 
SET `payment_status` = 'REFUNDED' 
WHERE `vnp_transaction_no` IS NOT NULL 
  AND `status` = 'CANCELLED';

-- 3. Tạo bảng lưu trữ lịch sử và audit log giao dịch thanh toán
CREATE TABLE IF NOT EXISTS `payment_transactions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `payment_method` VARCHAR(50) NOT NULL,
    `txn_ref` VARCHAR(100) NOT NULL,
    `transaction_no` VARCHAR(100) NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `bank_code` VARCHAR(50) NULL,
    `card_type` VARCHAR(50) NULL,
    `response_code` VARCHAR(10) NULL,
    `status` VARCHAR(20) NOT NULL,
    `pay_date` DATETIME NULL,
    `raw_response` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payment_transactions_order` 
        FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tạo Index tối ưu hóa hiệu năng truy vấn
CREATE INDEX `idx_payment_transactions_order_id` ON `payment_transactions` (`order_id`);
CREATE INDEX `idx_payment_transactions_txn_ref` ON `payment_transactions` (`txn_ref`);
