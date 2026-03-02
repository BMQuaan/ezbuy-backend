-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ezphone
-- ------------------------------------------------------
-- Server version	8.0.39

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

-- SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'e00c6362-6e7f-11ef-8e3e-546ceb294be9:1-1136';

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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (5,4,33,1),(7,2,11,1);
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
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_categories_parent` (`parent_id`),
  KEY `idx_categories_parent_active` (`parent_id`,`is_active`),
  CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Smartphones','https://res.cloudinary.com/doaswiru0/image/upload/v1761550332/categories/ltvhqxwbnctqzeldsh0c.png',1,NULL,'smartphones','2025-10-27 07:32:14','2025-10-27 07:32:14',NULL),(2,'Watches','https://res.cloudinary.com/doaswiru0/image/upload/v1761550358/categories/jjbos7u1lbtmwfo7mvbr.png',1,NULL,'watches','2025-10-27 07:32:41','2025-10-27 07:32:41',NULL),(3,'Headphones','https://res.cloudinary.com/doaswiru0/image/upload/v1761550391/categories/lbauuxulvfoqucwymozu.png',1,NULL,'headphones','2025-10-27 07:33:14','2025-10-27 07:33:14',NULL),(4,'Keyboards','https://res.cloudinary.com/doaswiru0/image/upload/v1761550563/categories/ebzrd5as44tl57wq33ra.png',1,NULL,'keyboards','2025-10-27 07:35:03','2025-10-27 07:36:06',NULL),(5,'Cameras','https://res.cloudinary.com/doaswiru0/image/upload/v1761550725/categories/pyxax6q4xktnybtzx9dx.png',0,NULL,'cameras','2025-10-27 07:38:48','2025-10-31 14:45:52','2025-10-31 14:45:52'),(6,'Mouses','https://res.cloudinary.com/doaswiru0/image/upload/v1761919812/categories/xx0bcp09e8yvbc7cr1m4.png',0,NULL,'mouses','2025-10-31 14:10:12','2025-11-01 13:52:01','2025-11-01 13:52:01'),(7,'luan2','https://res.cloudinary.com/doaswiru0/image/upload/v1761920945/categories/hfb5fdf4ehabhhwfkvdn.png',0,NULL,'luan2','2025-10-31 14:29:07','2025-10-31 14:43:19','2025-10-31 14:43:19');
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forgot_password`
--

LOCK TABLES `forgot_password` WRITE;
/*!40000 ALTER TABLE `forgot_password` DISABLE KEYS */;
INSERT INTO `forgot_password` VALUES (1,5,'$2a$10$005JbRPRmiq3z1PqE7BqGOI93pf706ZLW7IiQSkmScly2axhZ7gZq','2025-10-31 07:00:05','2025-10-31 07:10:05',0),(2,5,'$2a$10$D224F1QOfct9Ld0XZd7Zpe3MGU8RV.UpG1/at3cFj7P5hpLKdtuO2','2025-10-31 07:06:12','2025-10-31 07:16:12',0),(3,5,'$2a$10$AmvyFk1ZutCYBnsLzdcGI.pEq2dm6cmTgOJPBC9pBs0oHDAnC7que','2025-10-31 07:07:21','2025-10-31 07:17:21',0),(4,5,'$2a$10$zzeA53gjXjXd7j8RVSCRueGFGVCn/NE8/V7qSKTktX5EZuyZwPtL.','2025-10-31 07:14:33','2025-10-31 07:24:33',0),(5,5,'$2a$10$8eS4yrr.HEv5rMggfvnF/.9h/cN8dU5U7pDnMGYI0.5fAzexpkLNO','2025-10-31 07:15:32','2025-10-31 07:25:32',0),(6,5,'$2a$10$e96NRxpYiCDZFEtnVWtP6.D9Xj/gnpnotCj55B.Xq1xZ5U6/lhMBa','2025-10-31 07:16:08','2025-10-31 07:26:08',0),(7,5,'$2a$10$hSoIHpfcy7KiawAVDpR8BOCOifjnEsVutQvNFX3CvhncOH/1ErzDe','2025-10-31 07:18:23','2025-10-31 07:28:23',0),(8,5,'$2a$10$vpi/7yVrUjbB/5e5mzlvNOyT5JL.vz/3DuCDcIy9nhQkP4P5.iXoS','2025-10-31 07:21:53','2025-10-31 07:31:53',0),(9,5,'$2a$10$w3hS.tM.S5msw68ZbOuJeu/HSb2JS0Oy.9jIayXG.WgCFul5OP7wm','2025-10-31 08:02:43','2025-10-31 08:12:43',0),(10,2,'$2a$10$IBfX98.sWNXKoyeijoDqGOfvvU9wc2zNjLQESuQQxcwerMe6BFZay','2025-10-31 08:08:26','2025-10-31 08:18:26',0),(11,2,'$2a$10$7v/CNLmrhtE7mCCNng7o6e5miFMusiMfoy3inPe0uIXiLfU8/rhHa','2025-10-31 08:13:34','2025-10-31 08:23:34',0),(12,5,'$2a$10$FS3e8YOc1b9W/hrAhCZoYOAxC7fFYzpDrhY67Uax5ciJ3Bc29cISq','2025-10-31 15:35:28','2025-10-31 15:45:28',0),(13,2,'$2a$10$8slQpOcQrAjpW.iTQ0FOgeTge7NjePpheClGdQ8eXNhFUWG/TgeRG','2025-10-31 15:40:46','2025-10-31 15:50:46',0),(14,2,'$2a$10$TkT1UHTlezvU3DN1ZDqPYentsO3gxb16GKJ4Po/NTcfZHlppz0NrC','2025-10-31 15:40:56','2025-10-31 15:50:56',0),(15,2,'$2a$10$0M2/ueKff7Xt8PNYbvkDuu60U6nu6Y/UJ1boy89n/XfysNleBokdW','2025-10-31 15:51:02','2025-10-31 16:01:02',0),(16,2,'$2a$10$MulDpKbqS0..wGfTLpBbOODBhuFBy2zvRbIINumaRjffdQ5LxfG4a','2025-10-31 16:07:15','2025-10-31 16:17:15',0),(17,2,'$2a$10$K44M513dkzJr/b0wMEvN4eJMLFLpnPubZJxDN9Fw97tAMAtVwxc3e','2025-10-31 16:07:32','2025-10-31 16:17:32',0),(18,2,'$2a$10$FXRLn4.hCIm9krA/Q8fXW.zYMw.iXiL1iVYsWKmZKEdHGZKx3Zdsm','2025-10-31 16:10:42','2025-10-31 16:20:42',0),(19,5,'$2a$10$QOzFBifN6arvNTxcBkkujuWp3/BhmXcq7aW1VQqdmiDlt9m3ZfL9C','2025-10-31 16:11:14','2025-10-31 16:21:14',1),(20,5,'$2a$10$ySyEg0eOtdA9jJZbSp8PlOrBcWS7t2WbFKi3ewC/JCTN8gBLJy9aa','2025-10-31 16:15:11','2025-10-31 16:25:11',0),(21,5,'$2a$10$PcimTi70oex.lSNXWNH/vupbW7Pa7Eu3g4KRYQXhnEFOEl2yKTT46','2025-10-31 16:15:46','2025-10-31 16:25:46',1),(22,5,'$2a$10$zjG.nqOcoq25TmN20IIet.LRj3QuSjjq1YQFn3XuTb5Ks6Zv9Jl8.','2025-10-31 16:18:34','2025-10-31 16:28:34',1),(23,5,'$2a$10$p2KgrZfXmX/W43mpvXgRMuc8vQZoFPzEjqDNKY7AurV7qK0eBcsKW','2025-10-31 16:20:22','2025-10-31 16:30:22',1),(24,5,'$2a$10$YQWW.TceRxkT5YRSoX5XBu01EOSL7YlH1YOJK6ArgXwt63oh7UN1W','2025-11-01 13:35:10','2025-11-01 13:45:10',0),(25,5,'$2a$10$p.p6zOa5xWp.Ue75ouBujurBxPDSJ2YBSgYQbJMP2jvQ.yS9WIpFq','2025-11-02 10:54:05','2025-11-02 11:04:05',1);
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
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manufacturers`
--

LOCK TABLES `manufacturers` WRITE;
/*!40000 ALTER TABLE `manufacturers` DISABLE KEYS */;
INSERT INTO `manufacturers` VALUES (1,'Sony',1,'2025-10-27 07:55:13','2025-10-27 07:55:13',NULL),(2,'Apple',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(3,'Xiaomi',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(4,'Samsung',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(5,'Vivo',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(6,'Realme',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(7,'JBL',1,'2025-10-27 07:57:42','2025-10-27 07:57:42',NULL),(8,'Canon',1,'2025-10-27 08:55:26','2025-10-27 08:55:26',NULL),(9,'Fujifilm',1,'2025-10-27 08:55:26','2025-10-27 08:55:26',NULL),(10,'Corsair',1,'2025-10-27 13:11:58','2025-10-27 13:11:58',NULL),(11,'Logitech',1,'2025-10-27 13:11:58','2025-10-27 13:11:58',NULL),(12,'Razer',1,'2025-10-27 13:11:58','2025-10-27 13:11:58',NULL),(13,'Huawei',1,'2025-10-27 13:35:31','2025-10-27 13:35:31',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,2,1,1799.00,'Iphone 17 Promax 256GB Orange'),(2,1,42,1,200.00,'Apple Watch Ultra 3'),(3,2,46,1,50.00,'Huawei Watch GT 5'),(4,3,4,1,899.00,'Iphone 12 256GB Red'),(5,3,32,1,499.00,'Apple AirPods Max');
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
  `subtotal` decimal(15,2) NOT NULL DEFAULT '0.00',
  `discount_total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `tax_total` decimal(15,2) NOT NULL DEFAULT '0.00',
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,2,1,1,'Liam','22/4 Duong 236, Tang Nhon Phu, Ho Chi Minh','+84706312921','','2025-10-30 15:38:30','PENDING',1999.00,99.95,0.00,1899.05,NULL,NULL),(2,4,NULL,1,'Long Nguyen','22/4 Duong 8, Thu Duc, Ho Chi Minh','+84706312921','','2025-10-30 15:41:02','CANCELLED',50.00,0.00,0.00,50.00,NULL,NULL),(3,2,NULL,1,'Liam','22/4 Duong 236, Tang Nhon Phu, Ho Chi Minh','+84706312921','','2025-10-31 02:38:06','COMPLETED',1398.00,0.00,0.00,1398.00,1,'2025-10-31 14:54:43');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,'COD');
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
  `price` decimal(10,2) NOT NULL,
  `quantity_in_stock` int NOT NULL DEFAULT '0',
  `category_id` int NOT NULL,
  `manufacturer_id` int NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_products_category` (`category_id`),
  KEY `idx_products_manufacturer` (`manufacturer_id`),
  KEY `idx_products_active` (`is_active`),
  KEY `idx_products_not_deleted` (`deleted_at`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_products_manufacturer` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturers` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `products_chk_1` CHECK ((`price` >= 0)),
  CONSTRAINT `products_chk_2` CHECK ((`quantity_in_stock` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Sony A7III','The Sony A7 III delivers outstanding image quality, impressive speed, and reliable performance in a compact body. Featuring a 24.2MP full-frame sensor, 4K video recording, and advanced autofocus with 693 phase-detection points, it’s designed for both professionals and enthusiasts who demand versatility and precision in every shot.','https://res.cloudinary.com/doaswiru0/image/upload/v1761551911/products/ezdw5hre3dxpbyaa3b9e.png','sony-a7iii',3490.00,15,5,1,0,'2025-10-27 07:58:33','2025-10-31 14:45:42','2025-10-31 14:45:42'),(2,'Iphone 17 Promax 256GB Orange','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553266/products/of9mglndvd9abnxxweof.png','iphone-17-promax-256gb-orange',1799.00,29,1,2,1,'2025-10-27 08:21:08','2025-10-30 15:38:30',NULL),(3,'Iphone 15 Promax 512GB Silver','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553363/products/jf0i300ls54bpywxcehx.png','iphone-15-promax-512gb-silver',1249.00,35,1,2,1,'2025-10-27 08:22:46','2025-10-27 08:22:46',NULL),(4,'Iphone 12 256GB Red','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553418/products/rr2lia5ezupojoze6cji.png','iphone-12-256gb-red',899.00,9,1,2,1,'2025-10-27 08:23:41','2025-10-31 02:38:06',NULL),(5,'Realme C55 128GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553472/products/tekh0u1wmjwnzkykuxjd.png','realme-c55-128gb',499.00,5,1,6,1,'2025-10-27 08:24:35','2025-10-27 08:24:35',NULL),(6,'Realme C35 256GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553521/products/q2macxl89ao1quukemcz.png','realme-c35',399.00,30,1,6,1,'2025-10-27 08:25:23','2025-10-27 08:26:20',NULL),(7,'Samsung Galaxy A35 256GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553641/products/hk2wj5q9xrr8zbfacpox.png','samsung-galaxy-a35-256gb',599.00,15,1,4,1,'2025-10-27 08:27:24','2025-10-27 08:27:24',NULL),(8,'Samsung Galaxy S24 Ultra 1TB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553694/products/jdomewqsonajalwslevu.png','samsung-galaxy-s24-ultra-1tb',1499.00,20,1,4,1,'2025-10-27 08:28:16','2025-10-27 08:28:16',NULL),(9,'Samsung Galaxy S23','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553730/products/fpizxrrcuisl7aync7ey.png','samsung-galaxy-s23',999.00,15,1,4,1,'2025-10-27 08:28:52','2025-10-27 08:28:52',NULL),(10,'Vivo Y16 128GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553769/products/d9nqkcnaahdcnqa5btxt.png','vivo-y16-128gb',349.00,0,1,5,1,'2025-10-27 08:29:32','2025-10-27 08:29:32',NULL),(11,'Vivo V25 128GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553810/products/ooi93tejrvsxfjurtn1r.png','vivo-v25-128gb',399.00,10,1,5,1,'2025-10-27 08:30:13','2025-10-27 08:30:13',NULL),(12,'Vivo T1 44W 12GB','Stay connected and productive with powerful smartphones that combine sleek design, smooth performance, and advanced camera technology for your everyday needs.','https://res.cloudinary.com/doaswiru0/image/upload/v1761553872/products/evudidsgrenq9oydtsae.png','vivo-t1-44w-12gb',499.00,10,1,5,1,'2025-10-27 08:31:14','2025-10-27 08:31:14',NULL),(13,'Sony ZV1','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761554830/products/l3uphxdhikdsvzb75oav.png','sony-zv1',499.00,20,5,1,0,'2025-10-27 08:47:13','2025-10-31 14:45:36','2025-10-31 14:45:36'),(14,'Canon R50 V','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761555291/products/q00rxtto0sx0r1uy9fvz.png','canon-r50-v',1999.00,10,5,8,0,'2025-10-27 08:54:53','2025-10-31 14:45:29','2025-10-31 14:45:29'),(15,'Canon R100','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761555446/products/zqhkiryd8yg3fyibfrpi.png','canon-r100',1199.00,20,5,8,0,'2025-10-27 08:57:28','2025-10-31 14:45:22','2025-10-31 14:45:22'),(16,'Canon PowerShot G7 X Mark III','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761555569/products/bklf5vihbjlr4wtepdoj.png','canon-powershot-g7-x-mark-iii',1099.00,20,5,8,0,'2025-10-27 08:59:32','2025-10-31 14:45:16','2025-10-31 14:45:16'),(17,'Sony Alpha 9 III','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761555817/products/qhjjgt2aoz3dnbpf49dc.png','sony-alpha-9-iii',5299.00,5,5,1,0,'2025-10-27 09:03:39','2025-10-31 14:45:10','2025-10-31 14:45:10'),(18,'Sony Alpha 7C','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761555944/products/tq3jqcm8d3aec08w4xcg.png','sony-alpha-7c',1099.00,20,5,1,0,'2025-10-27 09:05:46','2025-10-31 14:44:58','2025-10-31 14:44:58'),(19,'Sony Alpha 6400','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761556057/products/sklt3q09cx90q5kr7nun.png','sony-alpha-6400',799.00,10,5,1,0,'2025-10-27 09:07:39','2025-10-31 14:44:44','2025-10-31 14:44:44'),(20,'Fujifilm X-E5','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761556298/products/ta01fdqpzokqv6kdu3l4.png','fujifilm-x-e5',1699.00,5,5,9,0,'2025-10-27 09:11:41','2025-10-31 14:44:31','2025-10-31 14:44:31'),(21,'Fujifilm X-T5','Capture every moment in stunning detail with advanced cameras that offer superior image quality, fast performance, and creative flexibility.','https://res.cloudinary.com/doaswiru0/image/upload/v1761556396/products/z6sw9vixfzpvsg2ipwzf.png','fujifilm-x-t5',1399.00,10,5,9,0,'2025-10-27 09:13:18','2025-10-31 14:44:12','2025-10-31 14:44:12'),(22,'Corsair K95 RGB Platinum XT','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761570785/products/ubxrr3pngwskxwmml3yl.png','corsair-k95-rgb-platinum-xt',199.00,10,4,10,1,'2025-10-27 13:13:07','2025-10-27 13:14:18',NULL),(23,'Corsair K70 CORE RGB Gaming','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761570826/products/zpgm8f3gbdxwilds4udh.png','corsair-k70-core-rgb-gaming',79.00,10,4,10,1,'2025-10-27 13:13:49','2025-10-27 13:13:49',NULL),(24,'Corsair K55 RGB PRO Gaming','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761570896/products/e1cciq54yjuwxttghvnp.png','corsair-k55-rgb-pro-gaming',89.00,20,4,10,1,'2025-10-27 13:14:59','2025-10-27 13:14:59',NULL),(25,'Logitech MX Keys S','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761570971/products/g7bgldthlnxrhtwx5wsx.png','logitech-mx-keys-s',55.00,30,4,11,1,'2025-10-27 13:16:13','2025-10-27 13:16:13',NULL),(26,'Logitech K380 Multi‑Device Bluetooth','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571089/products/ilochfvgnxettkzfv58h.png','logitech-k380-multidevice-bluetooth',35.00,25,4,11,1,'2025-10-27 13:18:12','2025-10-27 13:18:12',NULL),(27,'Logitech G513 LIGHTSYNC RGB Mechanical','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571129/products/el0d8uolapf1v7gb0jwn.png','logitech-g513-lightsync-rgb-mechanical',45.00,30,4,11,1,'2025-10-27 13:18:51','2025-10-27 13:18:51',NULL),(28,'Razer Huntsman V3 Pro Tenkeyless','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571171/products/xseadqm8b6hkjuhj63e9.png','razer-huntsman-v3-pro-tenkeyless',43.00,5,4,12,1,'2025-10-27 13:19:34','2025-10-27 13:19:34',NULL),(29,'RAZER Cynosa V2 Chroma','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571211/products/vg3qxpzyesmvyxkbizb8.png','razer-cynosa-v2-chroma',69.00,30,4,12,1,'2025-10-27 13:20:14','2025-10-27 13:20:14',NULL),(30,'Razer BlackWidow V4 X Wired Mechanical','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571262/products/fxn9ic0qx3cpeeiyo9g0.png','razer-blackwidow-v4-x-wired-mechanical',55.00,30,4,12,1,'2025-10-27 13:21:04','2025-10-27 13:21:04',NULL),(31,'Razer BlackWidow V3 Tenkeyless RGB','Experience comfort and precision with high-quality keyboards designed for work, gaming, and creativity — built to match your style and speed.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571422/products/nojxqsv1uyqtglykogna.png','razer-blackwidow-v3-tenkeyless-rgb',89.00,10,4,12,1,'2025-10-27 13:23:44','2025-10-27 13:23:44',NULL),(32,'Apple AirPods Max','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571597/products/fzbrlga5cefcjfhhaoqi.png','apple-airpods-max',499.00,9,3,2,1,'2025-10-27 13:26:39','2025-10-31 02:38:06',NULL),(33,'Apple AirPods Max 2024 USB‑C','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571664/products/qoz1mfpys4vgvrql94bq.png','apple-airpods-max-2024-usbc',450.00,10,3,2,1,'2025-10-27 13:27:46','2025-10-27 13:27:46',NULL),(34,'Sony WH‑XB900N','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571707/products/om991joxhkxlrefrduk7.png','sony-whxb900n',240.00,20,3,1,1,'2025-10-27 13:28:29','2025-10-27 13:28:29',NULL),(35,'Sony WH‑ULT900N','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571745/products/z8nzd3qaap6lehiclixz.png','sony-whult900n',299.00,10,3,1,1,'2025-10-27 13:29:08','2025-10-27 13:29:08',NULL),(36,'Sony WH‑CH720N','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571779/products/v0zqwktrcd4kneooiymh.png','sony-whch720n',340.00,10,3,1,1,'2025-10-27 13:29:45','2025-10-27 13:29:45',NULL),(37,'Sony WH‑CH520','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571820/products/gyfg2aova28zoms3kyyo.png','sony-whch520',340.00,20,3,1,1,'2025-10-27 13:30:23','2025-10-27 13:30:23',NULL),(38,'Sony WH‑1000XM4','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571859/products/zacjcoar2dksdiyvptxe.png','sony-wh1000xm4',240.00,20,3,1,1,'2025-10-27 13:31:01','2025-10-27 13:31:01',NULL),(39,'Sony WH‑1000XM5','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571891/products/bnyrtj3f3ymyhusgfvs2.png','sony-wh1000xm5',340.00,10,3,1,1,'2025-10-27 13:31:34','2025-10-27 13:31:34',NULL),(40,'Sony WH‑1000XM6','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571924/products/epfsxmrytvpuu8gexq3t.png','sony-wh1000xm6',400.00,10,3,1,1,'2025-10-27 13:32:06','2025-10-27 13:32:06',NULL),(41,'Mi Headphone','Enjoy immersive sound and all-day comfort with premium headphones that deliver rich audio quality and seamless connectivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761571985/products/ef2desny3whh14jytqxm.png','mi-headphone',200.00,10,3,3,1,'2025-10-27 13:33:08','2025-10-27 13:33:08',NULL),(42,'Apple Watch Ultra 3','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572357/products/lqectflh2fcp7anh0fw3.png','apple-watch-ultra-3',200.00,9,2,2,1,'2025-10-27 13:39:20','2025-10-30 15:38:30',NULL),(43,'Apple Watch Series 11','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572402/products/laggm7tbss7hu9ylmjlr.png','apple-watch-series-11',249.00,10,2,2,1,'2025-10-27 13:40:04','2025-10-27 13:40:04',NULL),(44,'Huawei Watch Ultimate','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572700/products/us2hbtvr1vxagdcieni9.png','huawei-watch-ultimate',120.00,10,2,13,1,'2025-10-27 13:45:03','2025-10-27 13:45:03',NULL),(45,'Huawei Watch Ultimate 2','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572761/products/okdl2tnryffuazjosbox.png','huawei-watch-ultimate-2',200.00,10,2,13,1,'2025-10-27 13:46:03','2025-10-27 13:46:03',NULL),(46,'Huawei Watch GT 5','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572837/products/otwpuzw5lvktvsgbgxfz.png','huawei-watch-gt-5',50.00,10,2,13,1,'2025-10-27 13:47:20','2025-10-31 14:56:35',NULL),(47,'Huawei Watch GT 6 Pro','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572883/products/zyg2hw9pjcnbldx3nud4.png','huawei-watch-gt-6-pro',90.00,20,2,13,1,'2025-10-27 13:48:05','2025-10-27 13:48:05',NULL),(48,'Samsung Galaxy Watch FE 40','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761572935/products/mn2erzzyme8rirwkmbhm.png','samsung-galaxy-watch-fe-40',200.00,10,2,4,1,'2025-10-27 13:48:57','2025-10-27 13:48:57',NULL),(49,'Samsung Galaxy Watch Ultra','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761573039/products/tbadnzpdjc60d51ieuxf.png','samsung-galaxy-watch-ultra',200.00,10,2,4,1,'2025-10-27 13:50:42','2025-10-27 13:52:04',NULL),(50,'Samsung Galaxy Watch 7 BT 44M','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761573111/products/upynqrea2hbfe4iuqs37.png','samsung-galaxy-watch-7-bt-44m',240.00,10,2,4,1,'2025-10-27 13:51:53','2025-10-27 13:51:53',NULL),(51,'Apple Watch SE 2 2022','Stay on top of your day with smart and stylish watches that blend modern design with innovative features for health, fitness, and productivity.','https://res.cloudinary.com/doaswiru0/image/upload/v1761573180/products/dpfiv3yprsyjikpgk2cd.png','apple-watch-se-2-2022',230.00,10,2,2,0,'2025-10-27 13:53:03','2025-10-31 14:44:16','2025-10-31 14:44:16');
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
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_promotions_active` (`is_active`),
  KEY `idx_promotions_date` (`start_date`,`end_date`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotions`
--

LOCK TABLES `promotions` WRITE;
/*!40000 ALTER TABLE `promotions` DISABLE KEYS */;
INSERT INTO `promotions` VALUES (1,'EZBUY','Use this voucher to shop from a wide range of products — from fashion and beauty to home essentials and electronics — all at great prices.',5.00,'2025-10-20 07:44:00','2025-11-20 07:45:00',1,'2025-10-27 07:46:36','2025-11-02 11:28:23',NULL),(2,'EZBUYHALLOWEEN','EZBUYHALLOWEEN – Spooky Deals, Scary Good Discounts! Enjoy up to 10% OFF on thousands of products this Halloween!',15.00,'2025-10-30 15:12:00','2025-11-30 15:12:00',1,'2025-10-31 15:12:37','2025-11-02 10:48:57',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tokens`
--

LOCK TABLES `tokens` WRITE;
/*!40000 ALTER TABLE `tokens` DISABLE KEYS */;
INSERT INTO `tokens` VALUES (1,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTAyMzksImV4cCI6MTc2MjE1NTAzOX0.X308661bxJMS-KZpIbKaJX1omnk_JP5v6Xsq87ahjJc',1,'2025-10-27 07:30:39','2025-11-03 07:30:39'),(2,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTAyOTEsImV4cCI6MTc2MjE1NTA5MX0.N_VLRHPNUX39ZcvU5EU5rlKCU7lZpN-WjQjVCVfoUik',1,'2025-10-27 07:31:31','2025-11-03 07:31:31'),(3,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTU1MDg2MywiZXhwIjoxNzYyMTU1NjYzfQ.m0QFdvMuK9jkbU_9o4cGdI-T9tGu9zyBABL04CKfr-0',1,'2025-10-27 07:41:04','2025-11-03 07:41:04'),(4,3,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJxdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTU1MDg5MCwiZXhwIjoxNzYyMTU1NjkwfQ.09FbSZ2dEdD3Qsngbw-dXq6mtQyKUx1892xtLbfo5Y0',1,'2025-10-27 07:41:30','2025-11-03 07:41:30'),(5,4,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb25nQGdtYWlsLmNvbSIsImlhdCI6MTc2MTU1MDk0OCwiZXhwIjoxNzYyMTU1NzQ4fQ.VPJiwraP3PqBL_F_VIeXfBsnQyKWnkSc1exYKkD1EM0',1,'2025-10-27 07:42:28','2025-11-03 07:42:28'),(6,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTEwMTEsImV4cCI6MTc2MjE1NTgxMX0.WtgS3bMOIr5E7v7k8feeXIlxvhUd3TqpYn60IVFm7nI',1,'2025-10-27 07:43:31','2025-11-03 07:43:31'),(7,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTE1NzcsImV4cCI6MTc2MjE1NjM3N30.gTxlNDsoDada7MGidUBQCLNGvDWX8pINcqnbD4LGDxo',1,'2025-10-27 07:52:58','2025-11-03 07:52:58'),(8,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTMxNjAsImV4cCI6MTc2MjE1Nzk2MH0.Rip2q04sFvvhuS7CjZD8Wl0icbqJjKDLMU2DAvEQfVE',1,'2025-10-27 08:19:20','2025-11-03 08:19:20'),(9,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTQ3ODIsImV4cCI6MTc2MjE1OTU4Mn0.DFrBtFDhI5FjZAapLzM3i0AnM5GXdJP8eaudcFemGbc',1,'2025-10-27 08:46:22','2025-11-03 08:46:22'),(10,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NTYxNjEsImV4cCI6MTc2MjE2MDk2MX0.gT1uRCcDR3PJBaELDZovu6Za0KQisqAfEpKEGcD8SKA',1,'2025-10-27 09:09:22','2025-11-03 09:09:22'),(11,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NzAzMTMsImV4cCI6MTc2MjE3NTExM30.j6YbuxTuLEqtyNqZU00asarqZ5U_jg2z7NVO-j3-vR4',1,'2025-10-27 13:05:14','2025-11-03 13:05:14'),(12,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NzIyOTQsImV4cCI6MTc2MjE3NzA5NH0.12GhficOmQ6yp19MhZ580D6B1TrkPXu1WvW7t7BbJGY',1,'2025-10-27 13:38:15','2025-11-03 13:38:15'),(13,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE1NzI0ODUsImV4cCI6MTc2MjE3NzI4NX0.sW7pWR-tm5ui4I5x4cnGnDPYbglb7yri40lAgVs9AOA',1,'2025-10-27 13:41:26','2025-11-03 13:41:26'),(14,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE2NTg2MTUsImV4cCI6MTc2MjI2MzQxNX0.TwCkOPo3yCHBTbKPWUCiscC9Ju6rYXKRZSxlWOyTVNw',1,'2025-10-28 13:36:55','2025-11-04 13:36:55'),(15,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE2NjA0OTgsImV4cCI6MTc2MjI2NTI5OH0.Cg-nRDVdL1eCoUtxWFR6g_qAKNdDw_FQ3yTaifkauEo',1,'2025-10-28 14:08:19','2025-11-04 14:08:19'),(16,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE2NjE0OTUsImV4cCI6MTc2MjI2NjI5NX0.QAW8Tv5FzS9JLuQEwA-b9qkxKyhu655NKAbr1EmYTkk',1,'2025-10-28 14:24:56','2025-11-04 14:24:56'),(17,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTgzODI2MCwiZXhwIjoxNzYyNDQzMDYwfQ.RTZbpqKQoxWBdpkjEkQc5XiRAxszK75OZGkyCTJVot8',1,'2025-10-30 15:31:00','2025-11-06 15:31:00'),(18,4,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsb25nQGdtYWlsLmNvbSIsImlhdCI6MTc2MTgzODgyNywiZXhwIjoxNzYyNDQzNjI3fQ.51-t1t-2z9BWFzDhmmfpOYefXQish2Q9yiqEChFaCCA',1,'2025-10-30 15:40:27','2025-11-06 15:40:27'),(19,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTgzODg4MSwiZXhwIjoxNzYyNDQzNjgxfQ.0tyDfp3k85jB1n44CUIDdwswiNrcNmnsmvDdDuDWCK8',1,'2025-10-30 15:41:22','2025-11-06 15:41:22'),(20,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE4Mzg4OTMsImV4cCI6MTc2MjQ0MzY5M30.ceOOBeFOaxPBTBCMtjLdKyCqW22fp67sa7ywz9I9Krs',1,'2025-10-30 15:41:33','2025-11-06 15:41:33'),(21,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE4MzkwOTEsImV4cCI6MTc2MjQ0Mzg5MX0.C6M-MjSNhYtwrMw_UkKOgYw8GJsoTko-y2l5Pu6O_oQ',1,'2025-10-30 15:44:52','2025-11-06 15:44:52'),(22,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTg3Nzg0OSwiZXhwIjoxNzYyNDgyNjQ5fQ.MEd93zWL7nfHikVpADBq-WJjf2ew21jf6pfYZbxNYXU',1,'2025-10-31 02:30:50','2025-11-07 02:30:50'),(23,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE4Nzg0MDYsImV4cCI6MTc2MjQ4MzIwNn0.IyeTvf7Myk0PHwojZNeQ1WSJlPdEUerWblYHn7B2IGM',1,'2025-10-31 02:40:07','2025-11-07 02:40:07'),(24,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxODkzOTcxLCJleHAiOjE3NjI0OTg3NzF9.s_0ntbtasDwW4u2k_5Sv47AEtRRqltz5FXjYpPojZL8',1,'2025-10-31 06:59:31','2025-11-07 06:59:31'),(25,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTkxOTM0NywiZXhwIjoxNzYyNTI0MTQ3fQ.-lVACvaz4mwK8OAmfR4Ip6sL1J4UpQUXBfVnFYag4Bc',1,'2025-10-31 14:02:28','2025-11-07 14:02:28'),(26,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTkxOTQxMiwiZXhwIjoxNzYyNTI0MjEyfQ.hIy60w4m0Krw6KQKzW0ZJRghvGDuOvBfGiTOqXym-Yw',1,'2025-10-31 14:03:33','2025-11-07 14:03:33'),(27,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5MTk2NDgsImV4cCI6MTc2MjUyNDQ0OH0.Cp0hOnVkImM-cnvF1yP72stGdeKt0g8u3bdFoDrhU8k',1,'2025-10-31 14:07:28','2025-11-07 14:07:28'),(28,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTkyMDEwOCwiZXhwIjoxNzYyNTI0OTA4fQ.SmlToUV6aeYdZeO9G3hws90V8SR1McoFgl-JY3fL7gs',1,'2025-10-31 14:15:08','2025-11-07 14:15:08'),(29,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTkyMDEyNywiZXhwIjoxNzYyNTI0OTI3fQ.SZGG7_H55jJ0C96q0-SaJuZeafmnXzm5Mbib0DjnAQE',1,'2025-10-31 14:15:28','2025-11-07 14:15:28'),(30,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5MjAxMzgsImV4cCI6MTc2MjUyNDkzOH0.HuNDjdGc1mJuBeHfc2pSnkg_DKEMU5m2o0I4axg2Q2Q',1,'2025-10-31 14:15:39','2025-11-07 14:15:39'),(31,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5MjEwMDUsImV4cCI6MTc2MjUyNTgwNX0.Mkr526yof64vH_QBop3IacCEclbCvezJ21a-IcNXOEI',1,'2025-10-31 14:30:05','2025-11-07 14:30:05'),(32,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5MjI3MDIsImV4cCI6MTc2MjUyNzUwMn0.2KIB54PHnhmWlloZ0wvVCLkdXVD9yi6VyGfC2nIjRRs',1,'2025-10-31 14:58:22','2025-11-07 14:58:22'),(33,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5MjI5NTMsImV4cCI6MTc2MjUyNzc1M30.shkGgZjHS8Z6SCs-diaNtC85MJscnW3wT9vgckbJwuY',1,'2025-10-31 15:02:34','2025-11-07 15:02:34'),(34,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxOTI3MTQ0LCJleHAiOjE3NjI1MzE5NDR9.4t1RAEPqxH30nvra73vU5yvcWNEfLVMNgGJ8o9Aucgk',1,'2025-10-31 16:12:25','2025-11-07 16:12:25'),(35,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxOTI3MzgxLCJleHAiOjE3NjI1MzIxODF9.J-0ApVu6A9Eo6lFrdvQLr6epM-ZzjK2nGopQgc9BFA8',1,'2025-10-31 16:16:21','2025-11-07 16:16:21'),(36,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxOTI3NTQzLCJleHAiOjE3NjI1MzIzNDN9.Ky-bOcb55Vfsft4oDbS4pWxu3yUpCwRNVi376dVhwfs',1,'2025-10-31 16:19:04','2025-11-07 16:19:04'),(37,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxOTI3NjUxLCJleHAiOjE3NjI1MzI0NTF9.LB27MItxnblYTt6ca_j5yXqNeFXL5CGA6mstZYyqvBk',1,'2025-10-31 16:20:51','2025-11-07 16:20:51'),(38,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYxOTI3Njk2LCJleHAiOjE3NjI1MzI0OTZ9.vCpX0u2kLDKCBf9lHPd2KMsH4miZJiAnfdd6YlDwxP0',1,'2025-10-31 16:21:37','2025-11-07 16:21:37'),(39,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjE5Njc5MTIsImV4cCI6MTc2MjU3MjcxMn0.Upz_gfQjpv99Y53cjax9qxHL12-dUgnWDdLmivnu2lU',1,'2025-11-01 03:31:52','2025-11-08 03:31:52'),(40,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MTk2ODE2MywiZXhwIjoxNzYyNTcyOTYzfQ.3XBLoomSBrWMmvTYL-3Jl7z5n_vp0CtV7zur8rasGT8',1,'2025-11-01 03:36:03','2025-11-08 03:36:03'),(41,6,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaW5oQGdtYWlsLmNvbSIsImlhdCI6MTc2MTk2ODU4MiwiZXhwIjoxNzYyNTczMzgyfQ.X9B7VzCzHUvvJqim7U6nx7w-hRs9az6SxRDsP5cB5ms',1,'2025-11-01 03:43:03','2025-11-08 03:43:03'),(42,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwMDUxMDksImV4cCI6MTc2MjYwOTkwOX0.eDyY7oZ89kZmRdHtPEUcJ0Qd8PpaWdz7I4R-xxO8Trw',1,'2025-11-01 13:51:49','2025-11-08 13:51:49'),(43,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjAwNzg2NSwiZXhwIjoxNzYyNjEyNjY1fQ.I9BMtCXmKgRXGsYUlD1kMum5sjke-MZosbylz3_hm-0',1,'2025-11-01 14:37:45','2025-11-08 14:37:45'),(44,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjAwNzk2NywiZXhwIjoxNzYyNjEyNzY3fQ.7qKJGgFO8RisE1rbrE-gRkvhk4Rqureh_WwgeS5ruDw',1,'2025-11-01 14:39:27','2025-11-08 14:39:27'),(45,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA0ODgyMiwiZXhwIjoxNzYyNjUzNjIyfQ.cB7fpTGn3-HvlSKGUCUcsVA7uVixKaxp4hF3DAoU6RA',1,'2025-11-02 02:00:23','2025-11-09 02:00:23'),(46,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA1MDgwOCwiZXhwIjoxNzYyNjU1NjA4fQ.MtKcr_mNpB20We-JcVl68_taWAR8aa_TlG-xah8nbG0',1,'2025-11-02 02:33:28','2025-11-09 02:33:28'),(47,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODA0MTksImV4cCI6MTc2MjY4NTIxOX0.YWQozmKsy7Z-enmcRWAOFgV7-CWacisrgIaUCsn51gs',1,'2025-11-02 10:46:59','2025-11-09 10:46:59'),(48,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODA3MTEsImV4cCI6MTc2MjY4NTUxMX0.QKyOUsB56B6BoTxI9E_z8nnJqroH55kJs3MaSGxGY2o',1,'2025-11-02 10:51:51','2025-11-09 10:51:51'),(49,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODA3NDQsImV4cCI6MTc2MjY4NTU0NH0.KMcRM3u7-YhodaUGb_PWciy-3hZqutOShMzbFst4z6w',1,'2025-11-02 10:52:25','2025-11-09 10:52:25'),(50,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MDc3NCwiZXhwIjoxNzYyNjg1NTc0fQ.lwiM1qu2zVWZPTZOxyPB2Uqo9KQRXW7CHEeoNjbrCqk',1,'2025-11-02 10:52:55','2025-11-09 10:52:55'),(51,5,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJidWlraW5obHVhbjAxMDFAZ21haWwuY29tIiwiaWF0IjoxNzYyMDgwODc0LCJleHAiOjE3NjI2ODU2NzR9.UQUN7JxXO7-7OYVtIzeSOgExgM5P2dZNkJ5MpS53DlE',0,'2025-11-02 10:54:35','2025-11-09 10:54:35'),(52,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MDkyMSwiZXhwIjoxNzYyNjg1NzIxfQ.neWOVQ0pBB-X6j1JB3wB7jqzT-6vC3EYetvcmJ8Mp3k',1,'2025-11-02 10:55:21','2025-11-09 10:55:21'),(53,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MDkzOSwiZXhwIjoxNzYyNjg1NzM5fQ.nbsVAbTUxQuE8Z_DZCcJlHO5EZ8JHIixUmRDHJHVy_E',1,'2025-11-02 10:55:40','2025-11-09 10:55:40'),(54,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODEyOTcsImV4cCI6MTc2MjY4NjA5N30.jDINSorkRF7iYZRy_54mvwaRpDh_qM5HjEzrrnjLPs0',1,'2025-11-02 11:01:37','2025-11-09 11:01:37'),(55,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODEzMDQsImV4cCI6MTc2MjY4NjEwNH0.cvO9g6_5ef6xSzw0ks0zLkDiYchaXI2_F-E5YAPA_O8',1,'2025-11-02 11:01:44','2025-11-09 11:01:44'),(56,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MTM0NywiZXhwIjoxNzYyNjg2MTQ3fQ.3CpW0Th1UUV0V0g_8QdBol85hJc6SjjRvWEBc4NQRcs',1,'2025-11-02 11:02:27','2025-11-09 11:02:27'),(57,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODE0NzksImV4cCI6MTc2MjY4NjI3OX0.EH0aW_v_QYMnUjuizaTXWBnVxw9PHV0P3x3H18uA5GA',1,'2025-11-02 11:04:39','2025-11-09 11:04:39'),(58,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODE0OTEsImV4cCI6MTc2MjY4NjI5MX0.8qDPR1lPZx1wnpNAUTJjVfIRUAg6QCW7D2vv8Dqe59Y',1,'2025-11-02 11:04:51','2025-11-09 11:04:51'),(59,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MTc0MSwiZXhwIjoxNzYyNjg2NTQxfQ.SNZV7XEYWig3uvzRI-PokSVU84-SCx1JArumk34h4aM',1,'2025-11-02 11:09:02','2025-11-09 11:09:02'),(60,2,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWFuQGdtYWlsLmNvbSIsImlhdCI6MTc2MjA4MTg2OSwiZXhwIjoxNzYyNjg2NjY5fQ.bso5lq2E_M1vSMud1vDZda8iA3cRuPzekycLes8jw-s',1,'2025-11-02 11:11:10','2025-11-09 11:11:10'),(61,1,'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3NjIwODI4NjksImV4cCI6MTc2MjY4NzY2OX0.LgLBhPt5JaAvy9jQ2IPbW9wIItQ0RKp6tGM2J2MPVSM',0,'2025-11-02 11:27:50','2025-11-09 11:27:50');
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
  `deleted_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `google_id` (`google_id`),
  KEY `fk_users_role` (`role_id`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1,'Admin','Ezbuy','admin@gmail.com',NULL,NULL,'$2a$10$amnipohQPm3EtF9.UHZ7fO.NtOetirE5TDxtDUJxMgvNg5BFGmXUu',NULL,'LOCAL',NULL,1,NULL,'2025-10-27 07:30:39'),(2,2,'Luan','Bui','luan@gmail.com','0706312921','97 Man Thien, Tang Nhon Phu, HCMC','$2a$10$1dwMGfbzkU8Vbs/L1336Ne.aEwBSd/JD4eYRiWSXsxDwDS2VDeCGa',NULL,'LOCAL',NULL,1,NULL,'2025-10-27 07:41:04'),(3,2,'Quan','Bui','quan@gmail.com',NULL,NULL,'$2a$10$DIalJc5kq3wD2w7UKanMlupqY6cIP4vtajluIsxf0saZZFG8eyS0u',NULL,'LOCAL',NULL,1,NULL,'2025-10-27 07:41:30'),(4,2,'Long','Nguyen','long@gmail.com',NULL,NULL,'$2a$10$rTjDKWME5KrwdIcypi7u0uufVytHVK5WYRfxNLZtboXEPzZd2iXXq',NULL,'LOCAL',NULL,1,NULL,'2025-10-27 07:42:28'),(5,2,'Liam','Bui','buikinhluan0101@gmail.com',NULL,NULL,'$2a$10$p6L7qhZ.uIt0zw1MzaahF.ZkW/lig/2F6RM/VFi3DzeBAn2RDYj02',NULL,'LOCAL',NULL,1,NULL,'2025-10-31 06:59:31'),(6,2,'Minh','Vu','minh@gmail.com',NULL,NULL,'$2a$10$1QKD8hTa8OM5pXZMG3NLMuwpLn4E/9hJyq9TebGm5rWd7UJv/XlJm',NULL,'LOCAL',NULL,1,NULL,'2025-11-01 03:43:03');
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

-- Dump completed on 2025-11-02 18:46:17
