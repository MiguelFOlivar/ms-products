}-- -------------------------------------------------------------------------
-- BASE DE DATOS Y TABLAS (MySQL Workbench)
-- -------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS products_ms
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
USE products_ms;
 
-- 1. Tabla de roles para control de acceso
CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);
 
-- 2. Tabla de products
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,      -- 1 = disponible, 0 = retirado del catálogo
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
 
-- Roles de prueba (solo si no existen)
INSERT INTO roles (role_name)
SELECT 'Administrador' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'Administrador');
INSERT INTO roles (role_name)
SELECT 'Cliente' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'Cliente');
 