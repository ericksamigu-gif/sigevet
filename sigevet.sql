-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sigevet
-- ------------------------------------------------------
-- Server version	26.7.0

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

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '5ff69297-b9b5-11f1-bea1-a085275edcd2:1-66';

--
-- Table structure for table `alergia`
--

DROP TABLE IF EXISTS `alergia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alergia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `categoria` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alergia`
--

LOCK TABLES `alergia` WRITE;
/*!40000 ALTER TABLE `alergia` DISABLE KEYS */;
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
  `diagnostico` text,
  `tratamiento` text,
  `vacunas` varchar(255) DEFAULT NULL,
  `alergias` varchar(255) DEFAULT NULL,
  `cita_id` int NOT NULL,
  `mascota_id` int NOT NULL,
  `veterinario_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cita_id` (`cita_id`),
  KEY `mascota_id` (`mascota_id`),
  CONSTRAINT `atencion_medica_ibfk_1` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`),
  CONSTRAINT `atencion_medica_ibfk_2` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `atencion_medica`
--

LOCK TABLES `atencion_medica` WRITE;
/*!40000 ALTER TABLE `atencion_medica` DISABLE KEYS */;
INSERT INTO `atencion_medica` VALUES (1,'2026-09-28','Paciente en buen estado general','Control preventivo y recomendaciones de alimentacion','Rabia','Ninguna conocida',1,1,NULL),(2,'2026-09-28','Gastritis leve','Dieta blanda y medicacion por 5 dias','Ninguna','Ninguna conocida',3,3,NULL);
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
  `estado` varchar(20) DEFAULT NULL,
  `mascota_id` int NOT NULL,
  `veterinario_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `mascota_id` (`mascota_id`),
  KEY `veterinario_id` (`veterinario_id`),
  CONSTRAINT `cita_ibfk_1` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`id`),
  CONSTRAINT `cita_ibfk_2` FOREIGN KEY (`veterinario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` VALUES (1,'2026-09-28','09:00:00','Control general','ATENDIDA',1,1),(2,'2026-09-28','10:00:00','Vacunacion','PROGRAMADA',2,2),(3,'2026-09-28','11:00:00','Problemas digestivos','ATENDIDA',3,1),(4,'2026-09-29','09:30:00','Control de peso','PROGRAMADA',4,2),(5,'2026-09-29','11:00:00','Desparasitacion','PROGRAMADA',5,1),(6,'2026-10-02','10:00:00','Revisión','PENDIENTE',3,2),(7,'2026-10-01','11:02:00','Consulta','PENDIENTE',1,1);
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
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `dni` varchar(20) DEFAULT NULL,
  `fecha_registro` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'Carlos','Ramirez','987654321','carlos@gmail.com','Av. Lima 123',NULL,'2026-09-29 20:24:46'),(2,'Lucia','Torres','986543210','lucia@gmail.com','Jr. Union 456',NULL,'2026-09-29 20:24:46'),(3,'Pedro','Flores','985432109','pedro@gmail.com','Av. Central 789',NULL,'2026-09-29 20:24:46'),(4,'Rosa','Castillo','984321098','rosa@gmail.com','Jr. Los Olivos 321',NULL,'2026-09-29 20:24:46');
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
  `nombre` varchar(100) NOT NULL,
  `especie` varchar(50) NOT NULL,
  `raza` varchar(100) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `genero` varchar(10) DEFAULT NULL,
  `peso` decimal(5,2) DEFAULT NULL,
  `cliente_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `mascota_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mascota`
--

LOCK TABLES `mascota` WRITE;
/*!40000 ALTER TABLE `mascota` DISABLE KEYS */;
INSERT INTO `mascota` VALUES (1,'Max','Perro','Labrador','2021-05-10','Macho',25.50,1),(2,'Luna','Gato','Siames','2022-08-15','Hembra',4.20,2),(3,'Rocky','Perro','Bulldog','2020-03-20','Macho',18.70,3),(4,'Mia','Perro','Poodle','2023-01-12','Hembra',7.80,4),(5,'Nala','Gato','Persa','2021-11-05','Hembra',5.10,1);
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
  `severidad` varchar(50) DEFAULT NULL,
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
  `titulo` varchar(150) NOT NULL,
  `tipo_reporte` varchar(50) DEFAULT NULL,
  `fecha_generacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `usuario_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `reporte_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reporte`
--

LOCK TABLES `reporte` WRITE;
/*!40000 ALTER TABLE `reporte` DISABLE KEYS */;
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
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(20) NOT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `estado` varchar(20) DEFAULT 'ACTIVO',
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'jlopez','123456','VETERINARIO','jlopez@sigevet.com','Juan','Lopez','ACTIVO','2026-09-29 20:24:46'),(2,'mgarcia','123456','VETERINARIO','mgarcia@sigevet.com','Maria','Garcia','ACTIVO','2026-09-29 20:24:46'),(3,'aperez','123456','RECEPCIONISTA','aperez@sigevet.com','Ana','Perez','ACTIVO','2026-09-29 20:24:46');
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
  `nombre` varchar(100) NOT NULL,
  `especie_destino` varchar(100) DEFAULT NULL,
  `descripcion` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vacuna`
--

LOCK TABLES `vacuna` WRITE;
/*!40000 ALTER TABLE `vacuna` DISABLE KEYS */;
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
  `dosis` varchar(100) DEFAULT NULL,
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
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22 11:59:41
