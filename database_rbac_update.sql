-- =============================================================================
-- SIGEVET: SCRIPT DE ACTUALIZACIÓN DE CONTROL DE ACCESO POR ROLES (RBAC)
-- Base de Datos: MySQL 8 (sigevet)
-- =============================================================================

USE sigevet;

-- 1. Asegurar que las columnas 'rol' y 'estado' de la tabla 'usuario' tengan valores y tipos estándar
ALTER TABLE usuario MODIFY COLUMN rol VARCHAR(50) NOT NULL DEFAULT 'RECEPCIONISTA';
ALTER TABLE usuario MODIFY COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

-- 2. Asegurar la existencia del Administrador General con clave BCrypt cifrada ("123456")
INSERT INTO usuario (username, password, nombre, apellido, rol, correo, estado)
VALUES (
    'admin',
    -- Hash BCrypt estándar de '123456'
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Administrador',
    'General',
    'ADMINISTRADOR',
    'admin@sigevet.com',
    'ACTIVO'
)
ON DUPLICATE KEY UPDATE 
    rol = 'ADMINISTRADOR',
    estado = 'ACTIVO';

-- 3. Asignación y verificación de roles para el personal registrado
UPDATE usuario SET rol = 'VETERINARIO', estado = 'ACTIVO' WHERE username IN ('jlopez', 'mgarcia');
UPDATE usuario SET rol = 'RECEPCIONISTA', estado = 'ACTIVO' WHERE username = 'aperez';

-- 4. Verificación de los usuarios y roles configurados
SELECT id, username, nombre, apellido, rol, correo, estado FROM usuario ORDER BY id ASC;
