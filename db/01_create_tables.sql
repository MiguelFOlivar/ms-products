-- -------------------------------------------------------------------------
-- 01_create_tables.sql
-- Base de datos y tablas (MySQL Workbench)
-- -------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS products_MS
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE products_MS;


-- 2. Tabla de Products
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
