-- =====================================================
-- Base de datos unificada: ecommerce
-- Tablas propias (nuestro equipo): categories, products
-- Tabla del otro equipo (simulada): users
-- =====================================================

CREATE DATABASE IF NOT EXISTS ecommerce
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Ojo: en minúsculas. En Linux MySQL/MariaDB distingue mayúsculas en nombres de BD.
USE ecommerce;

-- -----------------------------------------------------
-- Tablas propias
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
                                          id INT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(100) NOT NULL UNIQUE,
    active TINYINT(1) NOT NULL DEFAULT 1
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS products (
                                        id INT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    category_id INT NOT NULL,
    CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES categories(id)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Tabla del otro equipo (simulada, no la usamos en nuestra API)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id_user BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    paternal_lastname VARCHAR(100) NOT NULL,
    maternal_lastname VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(45) NOT NULL DEFAULT 'CLIENTE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    autenticado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_user),
    CONSTRAINT uk_users_email UNIQUE (email)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Datos simulados para users (contraseñas FALSAS, no son hashes reales)
-- -----------------------------------------------------
INSERT IGNORE INTO users
    (name, paternal_lastname, maternal_lastname, email, password, role)
VALUES
    ('Admin',  'Demo', 'Simulado', 'admin@demo.com',   'HASH_SIMULADO_ADMIN',   'ADMIN'),
    ('Cliente','Demo', 'Uno',      'cliente1@demo.com','HASH_SIMULADO_CLIENTE1','CLIENTE'),
    ('Cliente','Demo', 'Dos',      'cliente2@demo.com','HASH_SIMULADO_CLIENTE2','CLIENTE');