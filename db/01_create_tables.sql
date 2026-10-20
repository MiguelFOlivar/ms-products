-- =====================================================
-- Base de datos unificada: ecommerce
-- Tablas propias (nuestro equipo): categories, products, promotions,
--                                  promotion_products, stock_movements
-- Tabla del otro equipo (simulada): users
-- =====================================================

CREATE DATABASE IF NOT EXISTS ecommerce
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


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
-- Promociones y productos asociados
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS promotions (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          name VARCHAR(150) NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_promotions_active_dates (active, starts_at, ends_at)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promotion_products (
                                                  promotion_id BIGINT NOT NULL,
                                                  product_id INT NOT NULL,

                                                  PRIMARY KEY (promotion_id, product_id),
    INDEX idx_promotion_products_product (product_id),
    CONSTRAINT fk_promotion_products_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotions(id),
    CONSTRAINT fk_promotion_products_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Tabla del otro equipo (simulada, no la usamos en nuestra API)
-- Debe crearse ANTES de stock_movements (que la referencia)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS ecommerce.users (
                                               id_user BIGINT NOT NULL AUTO_INCREMENT,
                                               user_identifier VARCHAR(20) NULL,
    name VARCHAR(100) NOT NULL,
    paternal_lastname VARCHAR(100) NOT NULL,
    maternal_lastname VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(45) NOT NULL DEFAULT 'USUARIO',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,

    PRIMARY KEY (id_user),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_user_identifier UNIQUE (user_identifier)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Movimientos de inventario (depende de products y users)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_movements (
                                               id BIGINT NOT NULL AUTO_INCREMENT,
                                               product_id INT NOT NULL,
                                               movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    actor_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_stock_movements_product
    FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_movements_actor
    FOREIGN KEY (actor_id) REFERENCES users(id_user)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    old_price DECIMAL(10, 2) NOT NULL,
    new_price DECIMAL(10, 2) NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_price_history_product
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Datos simulados para users (contraseñas FALSAS, no son hashes reales)
-- -----------------------------------------------------
INSERT IGNORE INTO users
    (user_identifier, name, paternal_lastname, maternal_lastname, email, password, role, is_active,failed_login_attempts,locked_until)
VALUES
    ('ADM-001-A', 'Ana', 'García', 'López', 'admin.principal@example.com', 'admin123', 'ADMIN', TRUE, 0, NULL),
    ('ADM-002-A', 'Carlos', 'Hernández', 'Martínez', 'admin.dos@example.com', 'admin321', 'ADMIN', TRUE, 0, NULL),
    ('USR-005-I', 'Laura', 'Flores', 'Morales', 'cliente.uno@example.com', 'client123', 'CLIENTE', TRUE, 0, NULL),
    ('USR-006-I', 'Miguel', 'Vargas', 'Castro', 'cliente.dos@example.com', 'cliente321', 'CLIENTE', TRUE, 0, NULL);