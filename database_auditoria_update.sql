-- =============================================================================
-- SIGEVET: SCRIPT DE MIGRACIÓN PARA CAMPOS DE AUDITORÍA Y ELIMINACIÓN LÓGICA
-- Base de Datos: MySQL 8 (sigevet)
-- Autor: SIGEVET Dev Team
-- =============================================================================

USE sigevet;

-- 1. TABLA CLIENTE: Auditoría y borrado lógico
ALTER TABLE cliente 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 2. TABLA MASCOTA: Auditoría y borrado lógico
ALTER TABLE mascota 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 3. TABLA CITA: Auditoría y borrado lógico
ALTER TABLE cita 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 4. TABLA ATENCION_MEDICA: Auditoría y borrado lógico
ALTER TABLE atencion_medica 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 5. TABLA USUARIO: Auditoría y borrado lógico
ALTER TABLE usuario 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 6. TABLA REPORTE: Auditoría y borrado lógico
ALTER TABLE reporte 
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_creacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_creacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_modificacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_modificacion VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion DATETIME NULL,
    ADD COLUMN IF NOT EXISTS usuario_eliminacion VARCHAR(100) NULL;

-- 7. Regularización de registros existentes
UPDATE cliente SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;
UPDATE mascota SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;
UPDATE cita SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;
UPDATE atencion_medica SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;
UPDATE usuario SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;
UPDATE reporte SET activo = TRUE, fecha_creacion = COALESCE(fecha_creacion, NOW()), usuario_creacion = COALESCE(usuario_creacion, 'admin') WHERE activo IS NULL OR fecha_creacion IS NULL;

-- 8. Verificación de campos
SELECT 'cliente' AS tabla, COUNT(*) AS total_activos FROM cliente WHERE activo = TRUE
UNION ALL
SELECT 'mascota', COUNT(*) FROM mascota WHERE activo = TRUE
UNION ALL
SELECT 'cita', COUNT(*) FROM cita WHERE activo = TRUE
UNION ALL
SELECT 'atencion_medica', COUNT(*) FROM atencion_medica WHERE activo = TRUE
UNION ALL
SELECT 'usuario', COUNT(*) FROM usuario WHERE activo = TRUE;
