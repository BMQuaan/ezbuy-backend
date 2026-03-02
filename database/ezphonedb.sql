-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: ezphone
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'f153596f-4406-11f0-aac9-005056c00001:1-723';

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_product` (`user_id`,`product_id`),
  KEY `fk_carts_product` (`product_id`),
  CONSTRAINT `fk_carts_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_carts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `parent_id` int DEFAULT NULL,
  `slug` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_categories_created_by` (`created_by`),
  KEY `fk_categories_updated_by` (`updated_by`),
  KEY `fk_categories_deleted_by` (`deleted_by`),
  KEY `idx_categories_parent` (`parent_id`),
  KEY `idx_categories_parent_active` (`parent_id`,`is_active`),
  CONSTRAINT `fk_categories_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_categories_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_categories_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Headphones','http://example.com/iphone-new.jpg',1,NULL,'ien-thoai-iphone-moi-2025','2025-09-28 04:07:28',NULL,'2025-11-02 08:16:15',6,NULL,NULL),(2,'Watches',NULL,1,1,'samsung','2025-09-28 04:07:28',NULL,'2025-11-02 08:16:15',NULL,NULL,NULL),(3,'Smartphones',NULL,1,1,'google-pixel','2025-09-28 04:07:28',NULL,'2025-11-02 08:16:15',NULL,NULL,NULL),(4,'Keyboards',NULL,1,3,'xiaomi','2025-09-28 04:07:28',NULL,'2025-11-02 08:16:15',NULL,NULL,NULL),(5,'Điện thoại iPhone','http://example.com/iphone.jpg',1,4,'ien-thoai-iphone','2025-10-02 16:10:29',6,'2025-10-05 07:31:49',NULL,'2025-10-04 15:37:06',6),(12,'Phụ kiện',NULL,1,NULL,'phu-kien','2025-10-06 14:37:19',6,'2025-10-06 14:37:19',NULL,NULL,NULL),(14,'Phụ kiệnn','https://res.cloudinary.com/doaswiru0/image/upload/v1759761497/categories/lyma3nudyztvzblnngtt.jpg',1,NULL,'phu-kienn','2025-10-06 14:38:18',6,'2025-10-06 14:38:18',NULL,NULL,NULL),(15,'Máy tính bảng iPadd','https://res.cloudinary.com/doaswiru0/image/upload/v1759761688/categories/xacu9fbumptnzyjp46bq.jpg',1,NULL,'may-tinh-bang-ipadd','2025-10-06 14:39:22',6,'2025-10-26 11:58:18',6,NULL,NULL),(16,'Smartphones',NULL,1,NULL,'smartphones','2025-10-09 14:47:49',NULL,'2025-10-09 14:47:49',NULL,NULL,NULL),(17,'Android Phones',NULL,1,1,'android-phones','2025-10-09 14:47:49',NULL,'2025-10-09 14:47:49',NULL,NULL,NULL),(18,'iPhones',NULL,1,1,'iphones','2025-10-09 14:47:49',NULL,'2025-10-09 14:47:49',NULL,NULL,NULL),(19,'Phụ kiệnnn',NULL,0,NULL,'phu-kiennn','2025-10-26 07:09:17',NULL,'2025-10-26 07:56:23',NULL,NULL,NULL),(22,'Phụ kiệnnn',NULL,0,NULL,'phu-kiennn','2025-10-26 07:27:04',NULL,'2025-10-26 07:56:23',NULL,NULL,NULL),(23,'Phụ kiệnnn1',NULL,1,NULL,'phu-kiennn','2025-10-26 07:56:28',NULL,'2025-10-26 08:16:28',NULL,NULL,NULL),(24,'Phụ kiệnnn',NULL,1,NULL,'phu-kiennn-1','2025-10-26 08:17:33',NULL,'2025-10-26 08:17:33',NULL,NULL,NULL),(25,'Phụ kiện2',NULL,0,NULL,'phu-kien2','2025-10-26 11:12:25',NULL,'2025-10-26 11:12:58',NULL,NULL,NULL),(26,'Phụ kiện2',NULL,1,NULL,'phu-kien2','2025-10-26 11:13:01',NULL,'2025-10-26 11:13:01',NULL,NULL,NULL);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forgot_password`
--

DROP TABLE IF EXISTS `forgot_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forgot_password` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `otp_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NULL DEFAULT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_fp_user` (`user_id`),
  CONSTRAINT `fk_fp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forgot_password`
--

LOCK TABLES `forgot_password` WRITE;
/*!40000 ALTER TABLE `forgot_password` DISABLE KEYS */;
INSERT INTO `forgot_password` VALUES (3,7,'$2a$10$5kRNnFa2226xfQNpUtMcPO71MKIectb3GNKOZVEur9wyAIaI3CDG2','2025-10-17 15:30:59','2025-10-17 15:40:59',1);
/*!40000 ALTER TABLE `forgot_password` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manufacturers`
--

DROP TABLE IF EXISTS `manufacturers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manufacturers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `fk_manufacturers_created_by` (`created_by`),
  KEY `fk_manufacturers_updated_by` (`updated_by`),
  KEY `fk_manufacturers_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_manufacturers_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_manufacturers_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_manufacturers_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manufacturers`
--

LOCK TABLES `manufacturers` WRITE;
/*!40000 ALTER TABLE `manufacturers` DISABLE KEYS */;
INSERT INTO `manufacturers` VALUES (1,'Apple',1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(2,'Samsung',1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(3,'Google',1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(4,'Xiaomi',1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(22,'REDMI',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(23,'OPPO',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(24,'ONEPLUS',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(25,'VIVO',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(26,'REALME',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(27,'MOTOROLA',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL),(28,'INFINIX',1,'2025-10-09 14:48:10',NULL,'2025-10-09 14:48:10',NULL,NULL,NULL);
/*!40000 ALTER TABLE `manufacturers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_order_product` (`order_id`,`product_id`),
  KEY `fk_order_items_product` (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (3,3,5,2,31990000.00,'Samsung Galaxy S25 Ultra'),(4,3,3,2,24990000.00,'Google Pixel 9 Pro'),(5,4,3,2,24990000.00,'Google Pixel 9 Pro'),(6,5,3,2,24990000.00,'Google Pixel 9 Pro'),(7,6,2,2,31990000.00,'Samsung Galaxy S25 Ultra'),(8,7,2,4,31990000.00,'Samsung Galaxy S25 Ultra'),(9,8,2,2,31990000.00,'Samsung Galaxy S25 Ultra'),(10,9,4,9,22990000.00,'Xiaomi 15 Pro');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `promo_id` int DEFAULT NULL,
  `payment_id` int DEFAULT NULL,
  `receiver_name` varchar(100) NOT NULL,
  `shipping_address` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `order_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('PENDING','CONFIRMED','SHIPPING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `subtotal` decimal(15,2) NOT NULL,
  `discount_total` decimal(15,2) NOT NULL,
  `tax_total` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `confirmed_by` int DEFAULT NULL,
  `confirmed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_orders_promo` (`promo_id`),
  KEY `fk_orders_confirmed_by` (`confirmed_by`),
  KEY `fk_orders_payment` (`payment_id`),
  KEY `idx_orders_user_status` (`user_id`,`status`),
  KEY `idx_orders_date` (`order_date`),
  CONSTRAINT `fk_orders_confirmed_by` FOREIGN KEY (`confirmed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_promo` FOREIGN KEY (`promo_id`) REFERENCES `promotions` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,6,NULL,1,'Minh Trần','456 Customer Avenue, HCMC','0987654321','Giao hàng trong giờ hành chính','2025-09-28 04:07:28','CONFIRMED',57980000.00,0.00,0.00,57980000.00,6,'2025-10-09 11:54:05'),(3,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-08 12:19:14','PENDING',113960000.00,0.00,0.00,113960000.00,6,NULL),(4,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-09 02:27:03','PENDING',49980000.00,0.00,0.00,49980000.00,NULL,NULL),(5,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-09 02:28:09','PENDING',49980000.00,0.00,0.00,49980000.00,NULL,NULL),(6,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-09 02:42:07','CANCELLED',63980000.00,0.00,0.00,63980000.00,NULL,NULL),(7,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-24 10:31:03','PENDING',127960000.00,0.00,0.00,127960000.00,NULL,NULL),(8,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-24 15:30:50','PENDING',63980000.00,0.00,0.00,63980000.00,NULL,NULL),(9,6,NULL,1,'Nguyễn Văn A','123 Đường ABC, Phường X, Quận Y, TP.HCM','0909123456','Giao hàng trong giờ hành chính','2025-10-25 10:35:04','PENDING',45980000.00,0.00,0.00,45980000.00,NULL,NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `method` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,'Cash on Delivery (COD)'),(2,'Credit/Debit Card'),(3,'Momo E-Wallet');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` text,
  `image_url` varchar(255) DEFAULT NULL,
  `slug` varchar(255) NOT NULL,
  `ram_gb` int DEFAULT NULL,
  `internal_storage_gb` int DEFAULT NULL,
  `battery_capacity_mah` int DEFAULT NULL,
  `screen_size_inch` decimal(4,2) DEFAULT NULL,
  `operating_system` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity_in_stock` int NOT NULL DEFAULT '0',
  `category_id` int NOT NULL,
  `manufacturer_id` int NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_products_created_by` (`created_by`),
  KEY `fk_products_updated_by` (`updated_by`),
  KEY `fk_products_deleted_by` (`deleted_by`),
  KEY `idx_products_cat_price` (`category_id`,`price`),
  KEY `idx_products_manufacturer` (`manufacturer_id`),
  KEY `idx_products_active` (`is_active`),
  KEY `idx_products_not_deleted` (`deleted_at`),
  KEY `idx_products_ram` (`ram_gb`),
  KEY `idx_products_storage` (`internal_storage_gb`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_products_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_products_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_products_manufacturer` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturers` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_products_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `products_chk_1` CHECK ((`price` >= 0)),
  CONSTRAINT `products_chk_2` CHECK ((`quantity_in_stock` >= 0)),
  CONSTRAINT `products_chk_3` CHECK (((`ram_gb` is null) or (`ram_gb` >= 0))),
  CONSTRAINT `products_chk_4` CHECK (((`internal_storage_gb` is null) or (`internal_storage_gb` >= 0))),
  CONSTRAINT `products_chk_5` CHECK (((`battery_capacity_mah` is null) or (`battery_capacity_mah` >= 0))),
  CONSTRAINT `products_chk_6` CHECK (((`screen_size_inch` is null) or (`screen_size_inch` > 0)))
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'iPhone 16 Pro Max','The ultimate iPhone experience with the powerful A18 Pro chip and a stunning ProMotion display.','https://example.com/images/iphone-16-pro.jpg','iphone-16-pro-max',12,256,4852,6.90,'iOS 18',34990000.00,100,1,1,1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(2,'Samsung Galaxy S25 Ultra','Experience the future of mobile AI with the Galaxy S25 Ultra, featuring an advanced camera system and a dynamic AMOLED 2X display.','https://example.com/images/s25-ultra.jpg','samsung-galaxy-s25-ultra',16,512,5500,6.80,'Android 15',31990000.00,72,2,2,1,'2024-09-28 04:07:28',NULL,'2025-10-28 02:32:25',NULL,'2025-10-24 16:00:43',NULL),(3,'Google Pixel 9 Pro','The smartest smartphone camera, powered by Google AI. Capture perfect photos every time and enjoy a pure Android experience.','https://example.com/images/pixel-9-pro.jpg','google-pixel-9-pro',12,256,5050,6.70,'Android 15',24990000.00,114,3,3,1,'2025-09-28 04:07:28',NULL,'2025-11-02 07:59:09',NULL,'2025-10-08 01:53:36',6),(4,'Xiaomi 15 Pro','Flagship performance at an incredible price. The Xiaomi 15 Pro brings a Leica-engineered camera and blazing-fast charging.','https://example.com/images/xiaomi-15-pro.jpg','xiaomi-15-pro',16,512,5000,6.73,'Android 15 with HyperOS',22990000.00,148,4,4,1,'2025-09-27 04:07:28',NULL,'2025-10-28 02:32:09',NULL,NULL,NULL),(5,'iPhone 17 Pro Max 256GB','Siêu phẩm mới nhất từ Apple với chip A19 Bionic.','http://example.com/iphone17.jpg','iphone-17-pro-max-256gb',12,256,5000,6.90,'iOS 19',35990000.00,100,1,1,1,'2025-10-05 13:52:46',6,'2025-10-05 13:52:46',NULL,NULL,NULL),(6,'Samsung Galaxy S25 Ultra update','Điện thoại mới nhất từ Samsung','http://example.com/iphone17.jpg','iphone-17-pro-max-256gb-1',16,NULL,NULL,NULL,NULL,35000000.00,100,5,2,1,'2025-10-05 13:52:53',6,'2025-10-07 11:43:16',6,NULL,NULL),(7,'Samsung Galaxy S25 Ultra update','Điện thoại mới nhất từ Samsung',NULL,'iphone-17-pro-max-256gb-2',16,NULL,NULL,NULL,NULL,35000000.00,100,5,2,1,'2025-10-05 13:54:25',6,'2025-10-07 11:43:16',6,NULL,NULL),(8,'iPhone 17 Pro Max 512GB - Mới cập nhật','Siêu phẩm mới nhất từ Apple với chip A19 Bionic và bộ nhớ 512GB.','http://example.com/iphone17.jpg','iphone-17-pro-max-256gb-3',12,256,5000,6.90,'iOS 19',39990000.00,50,1,1,0,'2025-10-05 13:54:45',6,'2025-10-05 15:01:27',6,'2025-10-05 15:01:27',6),(9,'Samsung Galaxy S25 Ultra update','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-1',16,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-06 03:23:46',6,'2025-10-06 03:47:10',6,NULL,NULL),(16,'Phụ kiện11','Điện thoại mới nhất từ Samsung','https://res.cloudinary.com/doaswiru0/image/upload/v1759758680/products/cm8gm9oelvfmavmqcxga.jpg','phu-kien11',16,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-06 13:16:53',6,'2025-10-26 13:22:07',6,NULL,NULL),(139,'Samsung Galaxy S25 Ultra','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-3',NULL,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-26 08:24:07',NULL,'2025-10-26 08:24:07',NULL,NULL,NULL),(140,'Samsung Galaxy S25 Ultra','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-4',NULL,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-26 11:10:13',NULL,'2025-10-26 11:10:13',NULL,NULL,NULL),(141,'Samsung Galaxy S25 Ultra','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-5',NULL,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-26 11:10:17',NULL,'2025-10-26 11:10:17',NULL,NULL,NULL),(142,'Samsung Galaxy S25 Ultra','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-2',NULL,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-26 13:08:03',NULL,'2025-10-26 13:08:03',NULL,NULL,NULL),(143,'Samsung Galaxy S25 Ultra','Điện thoại mới nhất từ Samsung',NULL,'samsung-galaxy-s25-ultra-6',NULL,NULL,NULL,NULL,NULL,35000000.00,100,2,2,1,'2025-10-26 13:11:54',NULL,'2025-10-26 13:11:54',NULL,NULL,NULL);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotions`
--

DROP TABLE IF EXISTS `promotions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_value` decimal(10,2) NOT NULL,
  `start_date` timestamp NULL DEFAULT NULL,
  `end_date` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_promotions_created_by` (`created_by`),
  KEY `fk_promotions_updated_by` (`updated_by`),
  KEY `fk_promotions_deleted_by` (`deleted_by`),
  KEY `idx_promotions_active` (`is_active`),
  KEY `idx_promotions_date` (`start_date`,`end_date`),
  CONSTRAINT `fk_promotions_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_promotions_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_promotions_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
INSERT INTO `promotions` VALUES (1,'WELCOME10','Giảm 10% cho khách hàng mới',10.00,'2025-09-28 04:07:28','2025-10-28 04:07:28',1,'2025-09-28 04:07:28',NULL,'2025-09-28 04:07:28',NULL,NULL,NULL),(2,'BLACKFRIDAY','Giam gia lon dip Black Fridayy',25.50,'2025-11-19 17:00:00','2025-11-30 16:59:59',0,'2025-10-17 09:28:39',NULL,'2025-10-26 11:04:59',NULL,'2025-10-17 09:32:48',NULL),(5,'BLACKFRIDAY','Giam gia lon dip Black Friday',25.50,'2025-11-19 17:00:00','2025-11-30 16:59:59',0,'2025-10-26 11:05:43',NULL,'2025-10-26 11:06:10',NULL,NULL,NULL),(6,'BLACKFRIDAY','Giam gia lon dip Black Friday',25.50,'2025-11-19 17:00:00','2025-11-30 16:59:59',1,'2025-10-26 11:06:14',NULL,'2025-10-26 11:06:14',NULL,NULL,NULL);
/*!40000 ALTER TABLE `promotions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(2,'CUSTOMER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tokens`
--

DROP TABLE IF EXISTS `tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(255) NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_tokens_user` (`user_id`),
  CONSTRAINT `fk_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tokens`
--

LOCK TABLES `tokens` WRITE;
/*!40000 ALTER TABLE `tokens` DISABLE KEYS */;
INSERT INTO `tokens` VALUES (43,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk0MDM5MzIsImV4cCI6MTc2MDAwODczMn0.dHv5q8FSwAJW0-ofcuChwssZTka5laI1B9fJpB2cexs',1,'2025-10-02 11:18:52','2025-10-09 11:18:52'),(44,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk0MDUzMTgsImV4cCI6MTc2MDAxMDExOH0.--XZ_WXAtKJxKoxmhKCycHunD2ilXXmTq2Yh8_xkoPI',1,'2025-10-02 11:41:59','2025-10-09 11:41:59'),(45,7,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWVzdEBnbWFpbC5jb20iLCJpYXQiOjE3NTk0MDUzNjYsImV4cCI6MTc2MDAxMDE2Nn0.43AyxC4KYL8keEv5xY6QezA75L3yenzbb5EmDhZlrWo',1,'2025-10-02 11:42:46','2025-10-09 11:42:46'),(46,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk0MjEzMDUsImV4cCI6MTc2MDAyNjEwNX0.81yOelKMc1i-qnDo3q56qjEfjTIN5_s3BKFzEDcxETI',1,'2025-10-02 16:08:26','2025-10-09 16:08:26'),(47,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk1Njk2MDYsImV4cCI6MTc2MDE3NDQwNn0.SqlFqt8iLS0OSESJBEnU5SqfzTmhPRYWb2X3oGR2MqU',1,'2025-10-04 09:20:07','2025-10-11 09:20:07'),(48,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NzE1MTMsImV4cCI6MTc2MDE3NjMxM30.u1c9wg5clT2hM84CmVkyqJnGeTFn-_GDmEZmIMGA1vA',1,'2025-10-04 09:51:53','2025-10-11 09:51:53'),(49,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NzM0MzMsImV4cCI6MTc2MDE3ODIzM30.8BSJ3I_9FS7oQzU0qILg60ms9FeJqagCL-id7Y6UBBc',1,'2025-10-04 10:23:53','2025-10-11 10:23:53'),(50,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk1ODIyNzEsImV4cCI6MTc2MDE4NzA3MX0.xpPcjcqIbVsWkTw9vmN8kELdEAst1p9-y0MCC2QauMI',1,'2025-10-04 12:51:11','2025-10-11 12:51:11'),(51,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk1OTE3ODksImV4cCI6MTc2MDE5NjU4OX0.SOD0L0rb9xyi4U-xRvbmCmryRoXgE4khGJ4bG5por68',1,'2025-10-04 15:29:50','2025-10-11 15:29:50'),(52,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTE1NTUsImV4cCI6MTc2MDI1NjM1NX0.fQfu3Q-ZRyIWPrOKarJ4toDM622n5cs7SDKoXd3Rndw',1,'2025-10-05 08:05:56','2025-10-12 08:05:56'),(53,7,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWVzdEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NzIzMjUsImV4cCI6MTc2MDI3NzEyNX0.BmLdvULwrx9f1PWwofMZsC4etqQpIrtnA0LhxqreqZg',1,'2025-10-05 13:52:06','2025-10-12 13:52:06'),(54,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NzIzNDksImV4cCI6MTc2MDI3NzE0OX0.3jF6ueIXHwQApqDEg8t1y1iwvuEjHG0E_3MBfAV5h2A',1,'2025-10-05 13:52:30','2025-10-12 13:52:30'),(55,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NzQ0MTMsImV4cCI6MTc2MDI3OTIxM30.Kxoqs0-p90HbsIdxXs4bAJYhw1FibdKzHMxJLjPi6Ng',1,'2025-10-05 14:26:54','2025-10-12 14:26:54'),(56,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NzQ5MTksImV4cCI6MTc2MDI3OTcxOX0.bX97XeFAPETvSROjtg48zVCQ58dw-qKfFze_SToegj8',1,'2025-10-05 14:35:19','2025-10-12 14:35:19'),(57,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3MjA4NDksImV4cCI6MTc2MDMyNTY0OX0.CFJAomiT4s_BZ279EeRXG_Ur5QAGi0TVKAOAe0jHBso',1,'2025-10-06 03:20:49','2025-10-13 03:20:49'),(58,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3MjIxNzMsImV4cCI6MTc2MDMyNjk3M30.wCo-JHFLmOsyMjOn1jZQUNcAcWED6LW4_G2aY9kSa_c',1,'2025-10-06 03:42:54','2025-10-13 03:42:54'),(59,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3MjUwMDksImV4cCI6MTc2MDMyOTgwOX0.-6W8CIyttHd6CbCvW7_G_0c0nU5UyAZsKYl25SWuGXs',1,'2025-10-06 04:30:09','2025-10-13 04:30:09'),(60,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NDkwOTUsImV4cCI6MTc2MDM1Mzg5NX0.FjZelgSUQ3-E0zziIk6aCf9t9CWthDUmwXAw9lZyf8k',1,'2025-10-06 11:11:35','2025-10-13 11:11:35'),(61,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NTYyNDUsImV4cCI6MTc2MDM2MTA0NX0.weZYEEBAQQJMYIJFotl6xWegxfo_aKDb2MQWWGDAwoU',1,'2025-10-06 13:10:46','2025-10-13 13:10:46'),(62,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NTgyMzUsImV4cCI6MTc2MDM2MzAzNX0.qZ_VPqo1mrEhKpbeyIv-0Go19U2WdTVmysvnXweKZws',1,'2025-10-06 13:43:56','2025-10-13 13:43:56'),(63,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NTgyODUsImV4cCI6MTc2MDM2MzA4NX0.qq77WvjkqXdUmIkmivnnkjNduUHkB8KnqcylHqrtizY',1,'2025-10-06 13:44:45','2025-10-13 13:44:45'),(64,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NjA2MTgsImV4cCI6MTc2MDM2NTQxOH0.QV-8eGUoFs_RbpJSAg-y5gb-GyUY7pz4CpeCFyqFDGg',1,'2025-10-06 14:23:38','2025-10-13 14:23:38'),(65,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk3NjE2NTQsImV4cCI6MTc2MDM2NjQ1NH0.NsXLFy2_bo3ymgfMKt9Out4isP_NZdRtGkwRELWLwgk',1,'2025-10-06 14:40:55','2025-10-13 14:40:55'),(66,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk4NTEwMjQsImV4cCI6MTc2MDQ1NTgyNH0.arG_-NZQoPo5ONQMkJfRORNb9T8JcF9Fnl6O8yQfVtg',1,'2025-10-07 15:30:24','2025-10-14 15:30:24'),(67,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk4NTI4MzIsImV4cCI6MTc2MDQ1NzYzMn0.YmfyXO9c8VeOSXLnefpJUg2X9egOo7-bAjFYSFJRJRw',1,'2025-10-07 16:00:33','2025-10-14 16:00:33'),(68,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk4ODgzMjksImV4cCI6MTc2MDQ5MzEyOX0.-uNBPonAEgzZ8SBkInmu-bJ6LoR7PjeRY4Rcwz-jls0',1,'2025-10-08 01:52:10','2025-10-15 01:52:10'),(69,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk4OTAzMDYsImV4cCI6MTc2MDQ5NTEwNn0.3EHyx_YF9J4LBspmz6DNYH9wmNhEObcvS5JUpSbTAsU',1,'2025-10-08 02:25:07','2025-10-15 02:25:07'),(70,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk5MjQ4MzksImV4cCI6MTc2MDUyOTYzOX0.j4yDhCUT2dYXTPrsYj91GUFyGS6SAP3-g_E1XWU14Jo',1,'2025-10-08 12:00:39','2025-10-15 12:00:39'),(71,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NTk5NzY3NzMsImV4cCI6MTc2MDU4MTU3M30.RDSKTAatdl_NFuOqvhUSZ_EP6H3PbpzdpIc4TYhBC1g',1,'2025-10-09 02:26:14','2025-10-16 02:26:14'),(72,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjAwMTA3MDIsImV4cCI6MTc2MDYxNTUwMn0.sQJrNXgqv6Wa9L9l_xY2xxjr5VRrqP1b7a4HKCFwr50',1,'2025-10-09 11:51:43','2025-10-16 11:51:43'),(73,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjAxNzg1NTAsImV4cCI6MTc2MDc4MzM1MH0.2tH4p1IvzQttw0kaPhzN6aJaHHE-H1r7hjMjfpnCEys',1,'2025-10-11 10:29:11','2025-10-18 10:29:11'),(74,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjAxODEzODQsImV4cCI6MTc2MDc4NjE4NH0.7YB7iuXpBzqVFfpFYblmzLHS--knMR_3M8jGnjQcJq4',1,'2025-10-11 11:16:24','2025-10-18 11:16:24'),(75,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA2MjM5ODMsImV4cCI6MTc2MTIyODc4M30.bH7BmhvBlV3n-DhsXz0BQ9RCjcTu5uCDC1TeD3xK1Xw',1,'2025-10-16 14:13:04','2025-10-23 14:13:04'),(76,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA2OTIzMDEsImV4cCI6MTc2MTI5NzEwMX0.WYfMesprLIUeg4ccApdAEsPDkJCwCv4hRJO2oBn-eHQ',1,'2025-10-17 09:11:42','2025-10-24 09:11:42'),(77,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA2OTI1NTAsImV4cCI6MTc2MTI5NzM1MH0.Hat9-f52LutjP47YN237d0R7USuNqSaOheHLc-I2Wfo',1,'2025-10-17 09:15:50','2025-10-24 09:15:50'),(78,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA2OTMzMDUsImV4cCI6MTc2MTI5ODEwNX0.ScEqZfcHD5-rq00u2ynnuBKYGKqhmtlzeSw1JLAddGU',1,'2025-10-17 09:28:26','2025-10-24 09:28:26'),(79,7,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtcXVhbjM0N2JiYkBnbWFpbC5jb20iLCJpYXQiOjE3NjA3MTU0MjgsImV4cCI6MTc2MTMyMDIyOH0.bZaen-zNCuRAg5q3F5FiSCpk3oWPiGlXtKpRxVKr6Po',1,'2025-10-17 15:37:09','2025-10-24 15:37:09'),(80,7,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtcXVhbjM0N2JiYkBnbWFpbC5jb20iLCJpYXQiOjE3NjA3MTU1MzYsImV4cCI6MTc2MTMyMDMzNn0.IU_Qvweqaltg1nP59GEMsF7uik4f2jENdjNlrLtCYfc',0,'2025-10-17 15:38:57','2025-10-24 15:38:57'),(81,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA3ODk2NTksImV4cCI6MTc2MTM5NDQ1OX0.C80DPs4CaSgyKIPZ44ygLg9zHiKh435NaCFMFVnc26w',1,'2025-10-18 12:14:19','2025-10-25 12:14:19'),(82,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA5NTYyNjAsImV4cCI6MTc2MTU2MTA2MH0.gs42jlJ7tzK5X0qq4kDYY4b_BWD7ul_DY-4Xc_x4kR4',1,'2025-10-20 10:31:01','2025-10-27 10:31:01'),(83,8,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWVzdEBnbWFpbC5jb20iLCJpYXQiOjE3NjA5NTY5NTMsImV4cCI6MTc2MTU2MTc1M30.tos0MKIU7p0GCk1DU0IO21dAWsW1WPZ5zhXBeUP-uY0',0,'2025-10-20 10:42:33','2025-10-27 10:42:33'),(84,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA5NjA0MjQsImV4cCI6MTc2MTU2NTIyNH0.idip2nhNhOg22tj5aYVO7hk1RcLuuPd3iqSzMdnFda4',1,'2025-10-20 11:40:24','2025-10-27 11:40:24'),(85,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjA5NzE5MzIsImV4cCI6MTc2MTU3NjczMn0.5gb9aJj4WaXkzOiDIPAvt0pt13t9Ek2Os5sDxvT8oSg',1,'2025-10-20 14:52:13','2025-10-27 14:52:13'),(86,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwMzAwMDAsImV4cCI6MTc2MTYzNDgwMH0.yhhTz66CnWK2fG5GAswXI2QnRgJQw60Ie0cufJ_lYNA',1,'2025-10-21 07:00:00','2025-10-28 07:00:00'),(87,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwMzA3MzksImV4cCI6MTc2MTYzNTUzOX0.MYAB8bG97SS64nrHWNQk9T9OoujcGvYOeiH921Z_PvU',1,'2025-10-21 07:12:19','2025-10-28 07:12:19'),(88,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwMzA4ODgsImV4cCI6MTc2MTYzNTY4OH0.2HxqqzCFRNgOvawozJwufsw_aySG8yynXE7cicztjms',1,'2025-10-21 07:14:49','2025-10-28 07:14:49'),(89,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNDc4NTUsImV4cCI6MTc2MTY1MjY1NX0.kYyg6nW1-rva1dQeXLrWXYJE2wEWua7xpjXH0beXCVE',1,'2025-10-21 11:57:36','2025-10-28 11:57:36'),(90,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNDc4OTUsImV4cCI6MTc2MTY1MjY5NX0.pygjRzYWQYu4qUmjGGc34wMIbS_N5Ziau47GwnduXQA',1,'2025-10-21 11:58:15','2025-10-28 11:58:15'),(91,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNDg5MDUsImV4cCI6MTc2MTY1MzcwNX0.-dVk9Tff6qOzeQK_GkEgG6BTEo9h_69D2T3OiVQ7ZDg',1,'2025-10-21 12:15:06','2025-10-28 12:15:06'),(92,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNDk1MzEsImV4cCI6MTc2MTY1NDMzMX0.8Rlw2tsVqKpf7josiKujyC7maZuJabCiZeL9QqdEc2g',1,'2025-10-21 12:25:31','2025-10-28 12:25:31'),(93,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTE0NDEsImV4cCI6MTc2MTY1NjI0MX0.AbTDwnxzIwX3IRRmu4LX523j0YN6iE-zVo0QoUEeSsI',1,'2025-10-21 12:57:22','2025-10-28 12:57:22'),(94,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTE1MjAsImV4cCI6MTc2MTY1NjMyMH0.yyacOJaX9k6NN1ndYSvAElAMuUf-dw1_yD5wN0-R0Q4',1,'2025-10-21 12:58:40','2025-10-28 12:58:40'),(95,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTE1MjIsImV4cCI6MTc2MTY1NjMyMn0.E4JwjZvIPg5L1Oa95OOg5zxPTW0I02Vj7AZ7KpuQ5-4',1,'2025-10-21 12:58:43','2025-10-28 12:58:43'),(96,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTE1NzUsImV4cCI6MTc2MTY1NjM3NX0.Wmj1_l6tJ8Q73gT4TXwK716w7QDgNoJQZPe_edW9YCg',1,'2025-10-21 12:59:35','2025-10-28 12:59:35'),(97,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTE2MTIsImV4cCI6MTc2MTY1NjQxMn0.WaQ5iFkXkPgwbEC3yp98F6jQjPrmmbEAK1ORYvPnAas',1,'2025-10-21 13:00:13','2025-10-28 13:00:13'),(98,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTIzMzUsImV4cCI6MTc2MTY1NzEzNX0.-5FEb4pzxN_HgeuYVnFC7tPugm1ph2b0wbgaeYOyyTc',1,'2025-10-21 13:12:15','2025-10-28 13:12:15'),(99,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI0MjMsImV4cCI6MTc2MTY1NzIyM30.p9I8XbT0oNmH1sKqeYxF18XHzyqwVjhELx9QIRNkNhg',1,'2025-10-21 13:13:44','2025-10-28 13:13:44'),(100,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI0NDEsImV4cCI6MTc2MTY1NzI0MX0.IeHQakfkrfuTZuKNQaPC2psavuGR7qzxvp3t67q3jQg',1,'2025-10-21 13:14:01','2025-10-28 13:14:01'),(101,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI0NjMsImV4cCI6MTc2MTY1NzI2M30.aGcl_VuV_pXUnjyGCYbbHtMd_6zfQ4E7wDg7Brx0yGU',1,'2025-10-21 13:14:24','2025-10-28 13:14:24'),(102,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI0NzksImV4cCI6MTc2MTY1NzI3OX0.dcxS4U8Ne0nCuOtsjfKcNLlVuvs6v85yqvZzV360Qeo',1,'2025-10-21 13:14:39','2025-10-28 13:14:39'),(103,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI0OTgsImV4cCI6MTc2MTY1NzI5OH0.A8OAgJLkfad7siS2TycdHgmLflokjfADRlfh6G4Reqk',1,'2025-10-21 13:14:58','2025-10-28 13:14:58'),(104,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI1MTEsImV4cCI6MTc2MTY1NzMxMX0.torP-XFmPht5oOtige_sdlb5JVmGC5quG9BmiXs4ol0',1,'2025-10-21 13:15:11','2025-10-28 13:15:11'),(105,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI1MTUsImV4cCI6MTc2MTY1NzMxNX0.RKLgDDzpqc38t1beASZfGRAkeiHbvLmlKyp22yLcCZ0',1,'2025-10-21 13:15:16','2025-10-28 13:15:16'),(106,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI2MzIsImV4cCI6MTc2MTY1NzQzMn0.h_BE5IdtyoKDfvgYfwom_6SuVjjsj_Ic9jMrQ_cu3s0',1,'2025-10-21 13:17:13','2025-10-28 13:17:13'),(107,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI2MzQsImV4cCI6MTc2MTY1NzQzNH0.tsJw13M4Q9wLc8fImpkyLAQAzIBPvEGXEIbCges5FHY',1,'2025-10-21 13:17:14','2025-10-28 13:17:14'),(109,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTI4NDMsImV4cCI6MTc2MTY1NzY0M30.nm3KL-6YyB5skqafN2Gjx8RxJffcEOfEtafayC2-uyo',1,'2025-10-21 13:20:43','2025-10-28 13:20:43'),(110,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTM0OTksImV4cCI6MTc2MTY1ODI5OX0.Iu0ehg5q-4WNATiPMon2ECh-BLhT1O0-SOagJn6uzTw',1,'2025-10-21 13:31:39','2025-10-28 13:31:39'),(111,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEwNTM1OTgsImV4cCI6MTc2MTY1ODM5OH0.j7SYU5QE4WgAVVIFCYomvpJiLRpx2soenD1gVn-qDKY',1,'2025-10-21 13:33:18','2025-10-28 13:33:18'),(112,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjExMDUwODcsImV4cCI6MTc2MTcwOTg4N30.aX6OJ9fPB3uz-gJIuMxpoS1Flkqa9Vg8N4xxCG3jZvA',1,'2025-10-22 03:51:28','2025-10-29 03:51:28'),(113,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEyMjc3NDIsImV4cCI6MTc2MTgzMjU0Mn0.1ryc9KS2CffwCHMtzQut6znnPuG3bm8z0Mc5a4SGaXU',1,'2025-10-23 13:55:43','2025-10-30 13:55:43'),(114,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEyMzAxNjksImV4cCI6MTc2MTgzNDk2OX0.CtxwxK6JIiM6hxzp1eI7cX04CP-SoDLXOG7RlU31xW0',1,'2025-10-23 14:36:09','2025-10-30 14:36:09'),(115,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMDE4MzUsImV4cCI6MTc2MTkwNjYzNX0.jQ5EMqOMnGsCedbONDhSw2nBFqOei_TWR4I1KIDjcQo',1,'2025-10-24 10:30:36','2025-10-31 10:30:36'),(116,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMTI5MDgsImV4cCI6MTc2MTkxNzcwOH0.YThwm3UrTjY2ZiQbVuE9K6kMaXBmtarFo6_VCpiwPA0',1,'2025-10-24 13:35:09','2025-10-31 13:35:09'),(117,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMTM2NDQsImV4cCI6MTc2MTkxODQ0NH0.niPTqeCrI-Mmzy2zr-OOILEalAaecSa5mSUr9EiwNUY',1,'2025-10-24 13:47:24','2025-10-31 13:47:24'),(118,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMTQyNzIsImV4cCI6MTc2MTkxOTA3Mn0.NqKsnDEtZ8UuSNdPXo4vxygdHppb9A0AFlVm5CFYhfY',1,'2025-10-24 13:57:53','2025-10-31 13:57:53'),(119,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMTgzMTEsImV4cCI6MTc2MTkyMzExMX0.pTaMiMDXiLZbQV4z6p9IwIvtSJbzUbpLZ5-j59vVx18',1,'2025-10-24 15:05:11','2025-10-31 15:05:11'),(120,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMTk4MDUsImV4cCI6MTc2MTkyNDYwNX0.I3IIJ4EwTNiDXMtDR9g9T747r7bFMYnd7oFfX3IL_6Y',1,'2025-10-24 15:30:06','2025-10-31 15:30:06'),(121,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzMjE2MTksImV4cCI6MTc2MTkyNjQxOX0.raIJzg9p5OpkKqKlJFeVgEhA2yYfDfWlS-c3DqQxnhw',1,'2025-10-24 16:00:19','2025-10-31 16:00:19'),(122,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjEzODg0NjEsImV4cCI6MTc2MTk5MzI2MX0.ZjGIScCB35x48PdMzKNquvIDpiYMHR6ntrWuexwRdPY',1,'2025-10-25 10:34:21','2025-11-01 10:34:21'),(123,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0MDI5NTUsImV4cCI6MTc2MjAwNzc1NX0.Np1lGG8BW_JpVoOXBcOKCks3MdVJW4PC18EBny2kHOg',1,'2025-10-25 14:35:56','2025-11-01 14:35:56'),(124,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0MDI5NjIsImV4cCI6MTc2MjAwNzc2Mn0.aHv9ZsOMi2szvUi6FPEKQ7hvwJ5rSt6HPlJW0q5Ez64',1,'2025-10-25 14:36:03','2025-11-01 14:36:03'),(125,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0MDMwNzAsImV4cCI6MTc2MjAwNzg3MH0.t8GaNzOD1Sz4pNwqlUaiz5XdfV2rQNPEPy37bjrWwY4',1,'2025-10-25 14:37:51','2025-11-01 14:37:51'),(126,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0MDMwNzMsImV4cCI6MTc2MjAwNzg3M30.aopgE9MhJf-u7lxlib-y-bXlhAwpmhd0ZGefHywEf8c',1,'2025-10-25 14:37:54','2025-11-01 14:37:54'),(127,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0MDMwNzUsImV4cCI6MTc2MjAwNzg3NX0.JWBZw74HvFa0L--l5Y10gFgJXIGsUNPLZ0vks6FzW9Q',1,'2025-10-25 14:37:56','2025-11-01 14:37:56'),(128,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0NjI0OTYsImV4cCI6MTc2MjA2NzI5Nn0.Q41MJGRoEp_IcZs40R7yq1i0OIL-PvPZUV7CKvKaJXc',1,'2025-10-26 07:08:17','2025-11-02 07:08:17'),(129,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0NjUzNDYsImV4cCI6MTc2MjA3MDE0Nn0.6BRcbehUukNSraT-OpwNK1ONZihwt14iP2azH6wKvI8',1,'2025-10-26 07:55:47','2025-11-02 07:55:47'),(130,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0NjcwMzUsImV4cCI6MTc2MjA3MTgzNX0.qdifGqpOMzBTIvkbsBw_jGbj1UhSD9K75aEffeND79Y',1,'2025-10-26 08:23:56','2025-11-02 08:23:56'),(131,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0Njg5NjQsImV4cCI6MTc2MjA3Mzc2NH0.fga3Fq_MAI0oN0z0yGiwHNbzZ341oMZpXV5d85ubvQ4',1,'2025-10-26 08:56:04','2025-11-02 08:56:04'),(132,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0NzY2NzYsImV4cCI6MTc2MjA4MTQ3Nn0.FH03H04ke-NvLwVijMO-v9A08yCriNHOxA6dErj7c4M',1,'2025-10-26 11:04:37','2025-11-02 11:04:37'),(133,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0NzY5NzYsImV4cCI6MTc2MjA4MTc3Nn0.yaOLB5T5abN6h33mSdgNqTxGQ0TXZYMNnIDL9FV_2ic',1,'2025-10-26 11:09:36','2025-11-02 11:09:36'),(134,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0Nzk3MDUsImV4cCI6MTc2MjA4NDUwNX0.ugvosOvRPbiDHDMF_gkDH9-FzkwrPB_79aEl7XKn2zo',1,'2025-10-26 11:55:06','2025-11-02 11:55:06'),(135,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0ODE4NTksImV4cCI6MTc2MjA4NjY1OX0.Wg0PJWnA3hX8oYtNxZXuY5qHqyfcJh3TftP0i1i3D3c',1,'2025-10-26 12:31:00','2025-11-02 12:31:00'),(136,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0ODQwNjcsImV4cCI6MTc2MjA4ODg2N30.7Q_KwyEhCf0t2xodWnVRzr1tdeQkHIjiNHPJPd6CTkM',1,'2025-10-26 13:07:48','2025-11-02 13:07:48'),(137,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE0ODQ5MDEsImV4cCI6MTc2MjA4OTcwMX0.uY4N8zJJpyCid_5FJen6Ny3qMHSXGI7CKmBBY2-Ayow',1,'2025-10-26 13:21:42','2025-11-02 13:21:42'),(138,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwMDc5MjksImV4cCI6MTc2MjYxMjcyOX0.EjZPkndkzM-TwboXZXST92NBHnjyI7dNsrXOPB78X5I',1,'2025-11-01 14:38:50','2025-11-08 14:38:50'),(139,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwNzQwMDksImV4cCI6MTc2MjY3ODgwOX0.rwcdrcYN7vNkCXTHYBPdqFTMoEpDosPrQ9TlBVselqI',1,'2025-11-02 09:00:09','2025-11-09 09:00:09'),(140,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjI1Njc4MTAsImV4cCI6MTc2MzE3MjYxMH0.cyDIFiwIu5E7WD78oix2eqczMUYY4Q609P4SD-Afbn4',1,'2025-11-08 02:10:10','2025-11-15 02:10:10'),(141,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjI1Njc4MjMsImV4cCI6MTc2MzE3MjYyM30.zhKIzHmxsP38mPTBBe-qgy_R3jGX0F0uyhNy0ZNzd18',0,'2025-11-08 02:10:23','2025-11-15 02:10:23');
/*!40000 ALTER TABLE `tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(191) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `user_avatar` varchar(255) DEFAULT NULL,
  `login_type` enum('LOCAL','GOOGLE') NOT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `deleted_by` int DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `google_id` (`google_id`),
  KEY `fk_users_role` (`role_id`),
  KEY `fk_user_deleted_by` (`deleted_by`),
  CONSTRAINT `fk_user_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1,'Admin','User','admin@ezphone.com','0123456789','123 Admin St, HCMC','$2a$10$abcdef... (hashed_password)',NULL,'LOCAL',NULL,1,NULL,NULL,'2025-10-22 14:46:26'),(2,2,'John','Doe','john.doe@email.com','0987654321','456 Customer Ave, HCMC','$2a$10$ghijkl... (hashed_password)',NULL,'LOCAL',NULL,1,NULL,NULL,'2025-10-22 14:46:26'),(3,2,'Jane','Smith','jane.smith@google.com','0912345678','789 Client Rd, HCMC',NULL,NULL,'GOOGLE',NULL,1,NULL,NULL,'2025-10-22 14:46:26'),(5,2,'Quan','Bui','quan@gmail.com',NULL,NULL,'$2a$10$TSIR5DbJlkbkqCf5iYk3YewMhmJbG6zxd8HTWRKhxccYceCfwWodu',NULL,'LOCAL',NULL,0,NULL,NULL,'2025-09-30 06:40:26'),(6,1,'Admin','Mr.','admin@gmail.com','0987654321','456 Duong LMN, TP. HCM','$2a$10$FVRCiArBO69ns5B/sU580.euxEgxK2lUCNNb47rKEYhHBQ.jOOzKi','https://res.cloudinary.com/doaswiru0/image/upload/v1760692449/avatars/a0tjwjrjm6hxgu8gqoy6.jpg','LOCAL',NULL,1,NULL,NULL,'2025-09-30 06:46:11'),(7,2,'Mr','Guest','mquan347bbb@gmail.com',NULL,NULL,'$2a$10$y7jhKFNhr2elpKqZsXWkCe9lrguaDPDyKflmVA.nCLUgOkUgTur3O',NULL,'LOCAL',NULL,1,NULL,NULL,'2025-10-02 11:42:46'),(8,2,'Mr','Guest','guest@gmail.com',NULL,NULL,'$2a$10$NfSASLKW5fBSKDOFYBAh1.ogyTp5WVscw89p74ly0HApn8SHmqn7C',NULL,'LOCAL',NULL,1,NULL,NULL,'2025-10-20 10:42:33');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-12 20:45:35
