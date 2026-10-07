CREATE TABLE IF NOT EXISTS products (
                                        id INT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(150) NOT NULL,
                                        description TEXT NULL,
                                        price DECIMAL(10, 2) NOT NULL,
                                        stock INT NOT NULL,
                                        active TINYINT(1) NOT NULL DEFAULT 1,      -- 1 = disponible, 0 = retirado del catálogo
                                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
