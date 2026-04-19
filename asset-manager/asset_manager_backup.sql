-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: asset_manager
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `asset_audit_logs`
--

DROP TABLE IF EXISTS `asset_audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `new_state` varchar(255) DEFAULT NULL,
  `previous_state` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `asset_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpu2k04iql3isbgknrb96d776l` (`asset_id`),
  CONSTRAINT `FKpu2k04iql3isbgknrb96d776l` FOREIGN KEY (`asset_id`) REFERENCES `assets` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asset_audit_logs`
--

LOCK TABLES `asset_audit_logs` WRITE;
/*!40000 ALTER TABLE `asset_audit_logs` DISABLE KEYS */;
INSERT INTO `asset_audit_logs` VALUES (1,'CREATED','Assigned',NULL,'2026-03-26 07:13:07.158450','Sneha05',1),(2,'CREATED','Assigned',NULL,'2026-03-26 07:30:32.479491','Sneha05',2),(3,'CREATED','Available',NULL,'2026-04-04 16:09:31.068886','Sneha05',3),(4,'STATUS_CHANGE','Assigned','In Repair','2026-04-17 11:40:32.886482','Sneha05',1),(5,'STATUS_CHANGE','Assigned','In Repair','2026-04-17 12:20:47.888377','Sneha05',2),(6,'STATUS_CHANGE','Assigned','Available','2026-04-17 12:21:21.210659','Sneha05',3);
/*!40000 ALTER TABLE `asset_audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assets`
--

DROP TABLE IF EXISTS `assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_name` varchar(255) DEFAULT NULL,
  `bill_description` varchar(1000) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `purchase_date` date DEFAULT NULL,
  `assigned_employee_id` bigint DEFAULT NULL,
  `lifespan_months` int DEFAULT NULL,
  `purchase_price` decimal(10,2) DEFAULT NULL,
  `salvage_value` decimal(10,2) DEFAULT NULL,
  `health_status` varchar(255) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `repair_count` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi37wgdksbwub2fd1g8orrh975` (`user_id`),
  KEY `FKiqdbqdkmjud5ci5gltcohsq3j` (`assigned_employee_id`),
  CONSTRAINT `FKi37wgdksbwub2fd1g8orrh975` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKiqdbqdkmjud5ci5gltcohsq3j` FOREIGN KEY (`assigned_employee_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assets`
--

LOCK TABLES `assets` WRITE;
/*!40000 ALTER TABLE `assets` DISABLE KEYS */;
INSERT INTO `assets` VALUES (1,'IPHONE 17','Billing to XYZ.Pvt.Ltd','ADJA-FEFWE-8595',1,NULL,'Assigned','2026-03-14',7,4,299.00,39.00,'OPTIMAL',NULL,NULL,NULL,0),(2,'MacBook Pro M3','Billing to ABC.Pvt.Ltd','IFVI-2681-KJUD',1,NULL,'Assigned','2026-03-13',2,2,1999.00,199.00,'OPTIMAL',NULL,NULL,NULL,0),(3,'Dell Latitude 5420','Warranty 1 year','AHSD-2891-ASDW12',1,NULL,'Assigned','2026-04-01',2,1,999.00,149.00,'OPTIMAL',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `assets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_no` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `verification_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'9513578462','snj3507@gmail.com','Sneha Joshi','$2a$10$Te8NhfnucV4G59qLzVS5x.VkAHnU/o3XuRUSo7xZw5gtA7d90ZX5K','ROLE_ADMIN','Sneha05',_binary '',NULL),(2,'7539512684','khairatharva441@gmail.com','Atharva Khair','$2a$10$o5nTqcvoGuXUZBSEcID3cu1oWu9jjFUJMl.DaTAveGq04CRwq3rYy','ROLE_EMPLOYEE','atharva12',_binary '',NULL),(3,'9876543215','mayureshkhair22@gmail.com','Mayuresh Khair','$2a$10$vwBSdQDVEIeXSub/NVAU6uNg8h7gtNu0eVE17NkvnkjcpID4xG1y2','ROLE_ADMIN','Myuresh01',_binary '\0','613156'),(7,'7896541235','archana.khair@gmail.com','Archana Khair','$2a$10$U5tLi0OkYFW.Z5MEXwp26OnV0xksSIGhAu1x6gQFGlHSPMbchiUhu','ROLE_EMPLOYEE','Archana01',_binary '',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-19 10:37:43
