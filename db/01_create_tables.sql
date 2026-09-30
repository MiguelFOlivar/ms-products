-- -------------------------------------------------------------------------
-- 01_create_tables.sql
-- Base de datos y tablas (MySQL Workbench)
-- -------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS Products_MS
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE Products_MS;

-- 1. Tabla de Roles para control de acceso
CREATE TABLE IF NOT EXISTS Roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla de Products
CREATE TABLE IF NOT EXISTS Products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,      -- 1 = disponible, 0 = retirado del catálogo
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
