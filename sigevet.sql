-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: sigevet
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Current Database: `sigevet`
--

/*!40000 DROP DATABASE IF EXISTS `sigevet`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `sigevet` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `sigevet`;

--
-- Table structure for table `alergia`
--

DROP TABLE IF EXISTS `alergia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alergia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `categoria` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alergia`
--

LOCK TABLES `alergia` WRITE;
/*!40000 ALTER TABLE `alergia` DISABLE KEYS */;
INSERT INTO `alergia` (`id`, `nombre`, `categoria`) VALUES (1,'Penicilinas / Betalactámicos','Medicamentosa'),(2,'Picadura de Pulgas (DAPP)','Parásitos'),(3,'Proteína de Pollo / Aves','Alimentaria'),(4,'Ácaros del Polvo / Polen','Ambiental'),(5,'Antiinflamatorios AINEs','Medicamentosa'),(6,'Lácteos y Derivados','Alimentaria');
/*!40000 ALTER TABLE `alergia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `atencion_medica`
--

DROP TABLE IF EXISTS `atencion_medica`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `atencion_medica` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `diagnostico` varchar(255) DEFAULT NULL,
  `tratamiento` varchar(255) DEFAULT NULL,
  `vacunas` varchar(255) DEFAULT NULL,
  `alergias` varchar(255) DEFAULT NULL,
  `cita_id` int NOT NULL,
  `mascota_id` int NOT NULL,
  `veterinario_id` int DEFAULT NULL,
  `activo` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cita_id` (`cita_id`),
  KEY `mascota_id` (`mascota_id`),
  KEY `FK4ffy3i9prbgtpcsw7evb4ja5w` (`veterinario_id`),
  CONSTRAINT `atencion_medica_ibfk_1` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`),
  CONSTRAINT `atencion_medica_ibfk_2` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`id`),
  CONSTRAINT `FK4ffy3i9prbgtpcsw7evb4ja5w` FOREIGN KEY (`veterinario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `atencion_medica`
--

LOCK TABLES `atencion_medica` WRITE;
/*!40000 ALTER TABLE `atencion_medica` DISABLE KEYS */;
INSERT INTO `atencion_medica` (`id`, `fecha`, `diagnostico`, `tratamiento`, `vacunas`, `alergias`, `cita_id`, `mascota_id`, `veterinario_id`, `activo`, `fecha_creacion`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'2026-09-28','Paciente en buen estado general','Control preventivo y recomendaciones de alimentacion','Rabia','Ninguna conocida',1,1,NULL,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(2,'2026-09-28','Gastritis leve','Dieta blanda y medicacion por 5 dias','Ninguna','Ninguna conocida',3,3,NULL,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(3,'2026-11-15','Motivo de consulta: Control, vacuna anual.','asdasdasd','Ninguna','Ninguna conocida',11,1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `atencion_medica` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `mascota_id` int NOT NULL,
  `veterinario_id` int NOT NULL,
  `activo` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mascota_id` (`mascota_id`),
  KEY `veterinario_id` (`veterinario_id`),
  CONSTRAINT `cita_ibfk_1` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`id`),
  CONSTRAINT `cita_ibfk_2` FOREIGN KEY (`veterinario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` (`id`, `fecha`, `hora`, `motivo`, `estado`, `mascota_id`, `veterinario_id`, `activo`, `fecha_creacion`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'2026-09-28','09:00:00','Control general','ATENDIDA',1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(2,'2026-09-28','10:00:00','Vacunacion','ATENDIDA',2,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(3,'2026-09-28','11:00:00','Problemas digestivos','ATENDIDA',3,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(4,'2026-09-29','09:30:00','Control de peso','CANCELADA',4,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(5,'2026-09-29','11:00:00','Desparasitacion','PROGRAMADA',5,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(6,'2026-10-02','10:00:00','Revisión','PENDIENTE',3,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(7,'2026-10-01','11:02:00','Consulta','ATENDIDA',1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(8,'2026-10-26','06:35:00','Control, vacuna anual','PROGRAMADA',6,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(9,'2026-10-26','14:20:00','Control de peso','PROGRAMADA',7,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(10,'2026-10-28','22:58:00','Control, vacuna anual','ATENDIDA',3,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(11,'2026-11-15','14:00:00','Control, vacuna anual','ATENDIDA',1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(12,'2026-11-16','14:00:00','Control, vacuna anual','PROGRAMADA',1,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(13,'2026-11-26','08:00:00','Control, vacuna anual','PROGRAMADA',1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(16,'2026-11-26','09:00:00','Control, vacuna anual','PENDIENTE',3,1,_binary '','2026-11-25 21:05:29.685470',NULL,'2026-11-25 21:07:28.510477','admin',NULL,'admin'),(17,'2026-11-26','12:15:00','Control, vacuna anual','PROGRAMADA',1,1,_binary '','2026-11-25 21:44:07.442082',NULL,NULL,'admin',NULL,NULL),(18,'2026-11-26','12:30:00','Control, vacuna anual','PROGRAMADA',2,1,_binary '','2026-11-25 21:44:52.336138',NULL,NULL,'admin',NULL,NULL),(19,'2026-11-26','13:00:00','Control, vacuna anual','PROGRAMADA',4,2,_binary '','2026-11-25 22:02:50.076064',NULL,NULL,'admin',NULL,NULL);
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `dni` varchar(255) DEFAULT NULL,
  `fecha_registro` datetime DEFAULT CURRENT_TIMESTAMP,
  `activo` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` (`id`, `nombre`, `apellido`, `telefono`, `correo`, `direccion`, `dni`, `fecha_registro`, `activo`, `fecha_creacion`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'Carlos','Ramirez','987654321','carlos@gmail.com','Av. Lima 123',NULL,'2026-09-29 20:24:46',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Lucia','Torres','986543210','lucia@gmail.com','Jr. Union 456',NULL,'2026-09-29 20:24:46',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Pedro','Flores','985432109','pedro@gmail.com','Av. Central 789',NULL,'2026-09-29 20:24:46',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(4,'Rosa','Castillo','984321098','rosa@gmail.com','Jr. Los Olivos 321',NULL,'2026-09-29 20:24:46',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(5,'Eduardo','Mendoza','998877665','emendoza@gmail.com','Av. Los Fresnos 450','44556677','2026-10-26 00:02:11',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(6,'Anthony','Quispe','932654089','anthony.qe@gmail.com','av. xyz','46802564','2026-10-26 00:06:23',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(7,'Kimberly','Alban','923659742','kim@gmail.com','av. xybj','75023694','2026-10-26 01:13:37',_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(8,'Pedro','Ramirez','923659742','pedro.e@gmail.com','av. tghb','46803378','2026-10-27 21:45:12',_binary '',NULL,'2026-11-25 22:00:12.630836','2026-11-25 22:00:12.630836',NULL,'admin','admin'),(9,'Pedro','Ramirez','923659742','pedro.e@gmail.xn--com-khaa','av. tghb','4680337X','2026-11-18 21:48:48',_binary '',NULL,'2026-11-25 22:00:42.805207','2026-11-25 22:00:42.805207',NULL,'admin','admin'),(10,'Carlos','Ramirez','9533226','ejemplo@gmail.com','Av. 123','75023695','2026-11-18 21:58:47',_binary '',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mascota`
--

DROP TABLE IF EXISTS `mascota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mascota` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `especie` varchar(255) DEFAULT NULL,
  `raza` varchar(255) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `genero` varchar(255) DEFAULT NULL,
  `peso` double DEFAULT NULL,
  `cliente_id` int NOT NULL,
  `activo` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `mascota_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mascota`
--

LOCK TABLES `mascota` WRITE;
/*!40000 ALTER TABLE `mascota` DISABLE KEYS */;
INSERT INTO `mascota` (`id`, `nombre`, `especie`, `raza`, `fecha_nacimiento`, `genero`, `peso`, `cliente_id`, `activo`, `fecha_creacion`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'Max','Perro','Labrador','2021-05-10','Macho',25.5,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Luna','Gato','Siames','2022-08-15','Hembra',4.2,2,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Rocky','Perro','Bulldog','2020-03-20','Macho',18.7,3,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(4,'Mia','Perro','Poodle','2023-01-12','Hembra',7.8,4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(5,'Nala','Gato','Persa','2021-11-05','Hembra',5.1,1,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(6,'snow','Perro','Pequeño','2022-10-10','Macho',12,6,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(7,'Michi','Gato','Gato Criollo','2018-10-10','Hembra',11,7,_binary '',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `mascota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mascota_alergia`
--

DROP TABLE IF EXISTS `mascota_alergia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mascota_alergia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `mascota_id` int NOT NULL,
  `alergia_id` int NOT NULL,
  `severidad` varchar(255) DEFAULT NULL,
  `fecha_deteccion` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mascota_id` (`mascota_id`),
  KEY `alergia_id` (`alergia_id`),
  CONSTRAINT `mascota_alergia_ibfk_1` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`id`),
  CONSTRAINT `mascota_alergia_ibfk_2` FOREIGN KEY (`alergia_id`) REFERENCES `alergia` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mascota_alergia`
--

LOCK TABLES `mascota_alergia` WRITE;
/*!40000 ALTER TABLE `mascota_alergia` DISABLE KEYS */;
/*!40000 ALTER TABLE `mascota_alergia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reporte`
--

DROP TABLE IF EXISTS `reporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reporte` (
  `id` int NOT NULL AUTO_INCREMENT,
  `titulo` varchar(255) DEFAULT NULL,
  `tipo_reporte` varchar(255) DEFAULT NULL,
  `fecha_generacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `usuario_id` int DEFAULT NULL,
  `activo` bit(1) NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `reporte_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reporte`
--

LOCK TABLES `reporte` WRITE;
/*!40000 ALTER TABLE `reporte` DISABLE KEYS */;
INSERT INTO `reporte` (`id`, `titulo`, `tipo_reporte`, `fecha_generacion`, `usuario_id`, `activo`, `fecha_creacion`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'Reporte de Gestión (2026-11-01 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:24',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Reporte de Gestión (2026-11-01 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:27',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Reporte de Gestión (2026-11-01 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:28',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(4,'Reporte de Gestión (2026-11-01 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:31',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(5,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:47',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(6,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:48',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(7,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:48',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(8,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:48',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(9,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:48',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(10,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:49',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(11,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:49',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(12,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:54',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(13,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:54',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(14,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:55',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(15,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:55',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(16,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:55',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(17,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:55',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(18,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:55',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(19,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:01:57',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(20,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:10:52',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(21,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:10:58',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(22,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:12:27',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(23,'Exportación Excel Citas y Atenciones (2026-10-28 al 2026-11-26)','EXCEL_CITAS','2026-11-25 20:17:14',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(24,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:17:45',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(25,'Exportación Excel Citas y Atenciones (2026-10-28 al 2026-11-26)','EXCEL_CITAS','2026-11-25 20:26:44',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(26,'Exportación Excel Citas y Atenciones (2026-10-28 al 2026-11-26)','EXCEL_CITAS','2026-11-25 20:26:58',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL),(27,'Reporte de Gestión (2026-10-28 al 2026-11-26)','GERENCIAL_CITAS','2026-11-25 20:27:06',4,_binary '',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `reporte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `activo` bit(1) NOT NULL,
  `fecha_eliminacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `usuario_eliminacion` varchar(255) DEFAULT NULL,
  `usuario_modificacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` (`id`, `username`, `password`, `rol`, `correo`, `nombre`, `apellido`, `estado`, `fecha_creacion`, `activo`, `fecha_eliminacion`, `fecha_modificacion`, `usuario_creacion`, `usuario_eliminacion`, `usuario_modificacion`) VALUES (1,'jlopez','$2a$10$50h1vt7FnNwB9LeoYJhMieSkhjq3ijaiBWnVktOEXtsSfop9db/Wi','VETERINARIO','jlopez@sigevet.com','Juan','Lopez','ACTIVO','2026-09-29 20:24:46',_binary '',NULL,'2026-11-25 22:54:33.923252',NULL,NULL,'SISTEMA'),(2,'mgarcia','$2a$10$4ifiOau5TVb1.8S70DxpG.3qFoWYezFEewm4iEXD7V9jPqI.wOhLG','VETERINARIO','mgarcia@sigevet.com','Maria','Garcia','ACTIVO','2026-09-29 20:24:46',_binary '',NULL,'2026-11-25 22:54:33.997039',NULL,NULL,'SISTEMA'),(3,'aperez','$2a$10$4EDDjhd0MkFFfRxgWXNRMeOVw/TEdHrp40g0.enMt2eazkkyPBTNW','RECEPCIONISTA','aperez@sigevet.com','Ana','Perez','ACTIVO','2026-09-29 20:24:46',_binary '',NULL,'2026-11-25 22:54:34.072683',NULL,NULL,'SISTEMA'),(4,'admin','$2a$10$mPPUo4I0w825AeyoCLFnLu/1DrZeGGRJlQIHK5Dsq34KTWMb/QY0y','ADMINISTRADOR','admin@sigevet.com','Administrador','General','ACTIVO','2026-11-15 21:51:32',_binary '',NULL,'2026-11-25 22:54:33.831129',NULL,NULL,'SISTEMA'),(6,'jlopez2','$2a$10$U5kPcqjOq0Q1qZFHFxuL9.xgjUOmzM5NOMctYkcqymTPo/YFB.ZiS','RECEPCIONISTA','anthony.qe@gmail.com','Jesus','Lopez','ACTIVO','2026-11-25 21:28:17',_binary '',NULL,'2026-11-25 21:58:54.934365','admin',NULL,'admin');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vacuna`
--

DROP TABLE IF EXISTS `vacuna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vacuna` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `especie_destino` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vacuna`
--

LOCK TABLES `vacuna` WRITE;
/*!40000 ALTER TABLE `vacuna` DISABLE KEYS */;
INSERT INTO `vacuna` (`id`, `nombre`, `especie_destino`, `descripcion`) VALUES (1,'Antirrábica','Canino/Felino','Vacuna obligatoria anual contra el virus de la rabia.'),(2,'Séxtuple Canina (DHPPi+L)','Canino','Protege contra Moquillo, Parvovirus, Hepatitis, Parainfluenza y Leptospirosis.'),(3,'Parvovirus Canino','Canino','Inmunización temprana contra parvovirosis.'),(4,'Triple Felina (FVRCP)','Felino','Protege contra Rinotraqueítis, Calicivirus y Panleucopenia felina.'),(5,'Leucemia Felina (FeLV)','Felino','Protege contra el virus de la leucemia felina.'),(6,'KC Tos de las Perreras (Bordetella)','Canino','Prevención de la traqueobronquitis infecciosa canina.');
/*!40000 ALTER TABLE `vacuna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vacuna_aplicada`
--

DROP TABLE IF EXISTS `vacuna_aplicada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vacuna_aplicada` (
  `id` int NOT NULL AUTO_INCREMENT,
  `atencion_medica_id` int NOT NULL,
  `vacuna_id` int NOT NULL,
  `fecha_aplicacion` date NOT NULL,
  `dosis` varchar(255) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `atencion_medica_id` (`atencion_medica_id`),
  KEY `vacuna_id` (`vacuna_id`),
  CONSTRAINT `vacuna_aplicada_ibfk_1` FOREIGN KEY (`atencion_medica_id`) REFERENCES `atencion_medica` (`id`),
  CONSTRAINT `vacuna_aplicada_ibfk_2` FOREIGN KEY (`vacuna_id`) REFERENCES `vacuna` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vacuna_aplicada`
--

LOCK TABLES `vacuna_aplicada` WRITE;
/*!40000 ALTER TABLE `vacuna_aplicada` DISABLE KEYS */;
/*!40000 ALTER TABLE `vacuna_aplicada` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'sigevet'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-25 22:57:32
