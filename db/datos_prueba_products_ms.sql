-- -------------------------------------------------------------------------
-- DATOS DE PRUEBA para Products_MS (MySQL Workbench)
-- Ejecutar DESPUÉS de products_mysql.sql (necesita las tablas Roles y Products)
-- Contenido: tabla Users + 10 usuarios + 50 productos
-- -------------------------------------------------------------------------
USE Products_MS;

-- -------------------------------------------------------------------------
-- Tabla de Users (ligada a Roles)
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,            -- SHA-256 en hexadecimal
    role_id INT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES Roles(id)
);

-- -------------------------------------------------------------------------
-- 10 USUARIOS (2 administradores y 8 clientes)
-- Contraseñas de prueba: Admin123! para administradores, Cliente123! para clientes
-- INSERT IGNORE evita error si el correo ya existe (se puede re-ejecutar)
-- -------------------------------------------------------------------------
INSERT IGNORE INTO Users (full_name, email, password_hash, role_id) VALUES
('Carlos Ramírez Soto',      'carlos.ramirez@tienda.com',   SHA2('Admin123!', 256),   (SELECT id FROM Roles WHERE role_name = 'Administrador')),
('Mariana López Herrera',    'mariana.lopez@tienda.com',    SHA2('Admin123!', 256),   (SELECT id FROM Roles WHERE role_name = 'Administrador')),
('Juan Pérez Aguilar',       'juan.perez@correo.com',       SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Sofía Hernández Cruz',     'sofia.hernandez@correo.com',  SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Miguel Ángel Torres',      'miguel.torres@correo.com',    SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Daniela Morales Vega',     'daniela.morales@correo.com',  SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Luis Fernando Castillo',   'luis.castillo@correo.com',    SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Valeria Ortiz Mendoza',    'valeria.ortiz@correo.com',    SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Ricardo Gómez Navarro',    'ricardo.gomez@correo.com',    SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente')),
('Ana Karen Jiménez Ruiz',   'ana.jimenez@correo.com',      SHA2('Cliente123!', 256), (SELECT id FROM Roles WHERE role_name = 'Cliente'));

-- -------------------------------------------------------------------------
-- 50 PRODUCTOS (precios en MXN)
-- Nota: este bloque NO es re-ejecutable; si lo corres dos veces se duplican.
-- Para limpiar antes de volver a cargar:
--   DELETE FROM Products; ALTER TABLE Products AUTO_INCREMENT = 1;
-- Incluye algunos con stock = 0 y 3 retirados (active = 0) para probar casos.
-- -------------------------------------------------------------------------
INSERT INTO Products (name, description, price, stock, active) VALUES
-- Computación
('Laptop Lenovo IdeaPad 15',        'Laptop 15.6" Intel Core i5, 8 GB RAM, 512 GB SSD',        13499.00, 12, 1),
('Laptop HP Pavilion 14',           'Laptop 14" Ryzen 5, 16 GB RAM, 512 GB SSD',                15999.99,  8, 1),
('Laptop Dell Inspiron 15',         'Laptop 15.6" Intel Core i7, 16 GB RAM, 1 TB SSD',          19999.00,  5, 1),
('Monitor Samsung 24"',             'Monitor Full HD IPS 75 Hz, HDMI',                           2899.00, 20, 1),
('Monitor LG UltraWide 29"',        'Monitor ultrawide 2560x1080, AMD FreeSync',                 4599.00,  9, 1),
('Teclado mecánico Redragon Kumara','Teclado mecánico retroiluminado, switches azules',           899.00, 35, 1),
('Mouse Logitech M185',             'Mouse inalámbrico 2.4 GHz',                                  249.00, 60, 1),
('Mouse gamer Logitech G203',       'Mouse gamer RGB 8000 DPI',                                   549.00, 28, 1),
('Webcam Logitech C920',            'Cámara web Full HD 1080p con micrófono',                    1599.00, 14, 1),
('Disco duro externo 1 TB',         'Disco portátil USB 3.0 de 1 TB',                            1199.00, 25, 1),
-- Almacenamiento y accesorios
('Memoria USB Kingston 64 GB',      'Memoria USB 3.2 de 64 GB',                                   159.00, 80, 1),
('SSD Kingston NV2 500 GB',         'SSD NVMe M.2 de 500 GB',                                     899.00, 30, 1),
('Memoria RAM Kingston 8 GB DDR4',  'Módulo DDR4 3200 MHz para laptop',                           649.00, 22, 1),
('Hub USB-C 7 en 1',                'Adaptador HDMI, USB 3.0, SD y carga PD',                     599.00, 40, 1),
('Mochila para laptop 15.6"',       'Mochila antirrobo con puerto de carga USB',                  499.00, 33, 1),
-- Celulares y audio
('Smartphone Samsung Galaxy A15',   'Pantalla 6.5", 128 GB, cámara 50 MP',                       4299.00, 18, 1),
('Smartphone Motorola Moto G54',    'Pantalla 6.5" 120 Hz, 256 GB, 5G',                          4999.00, 15, 1),
('Smartphone Xiaomi Redmi Note 13', 'Pantalla AMOLED 6.67", 256 GB',                             5499.00, 11, 1),
('Audífonos Sony WH-CH520',         'Audífonos Bluetooth on-ear, 50 h de batería',                 899.00, 26, 1),
('Audífonos JBL Tune 510BT',        'Audífonos inalámbricos con graves profundos',                 999.00, 24, 1),
('Bocina JBL Flip 6',               'Bocina Bluetooth portátil resistente al agua',               2499.00, 16, 1),
('Cargador rápido 20W USB-C',       'Cargador de pared compatible con iPhone y Android',           299.00, 70, 1),
('Power bank Anker 10,000 mAh',     'Batería portátil de carga rápida',                            699.00, 38, 1),
-- Hogar y cocina
('Licuadora Oster 600 W',           'Licuadora de 3 velocidades con vaso de vidrio',              1099.00, 17, 1),
('Cafetera Black+Decker 12 tazas',  'Cafetera programable con filtro permanente',                  799.00, 21, 1),
('Horno de microondas Daewoo 20 L', 'Microondas con 6 niveles de potencia',                       1899.00,  9, 1),
('Freidora de aire Ninja 4 L',      'Freidora de aire sin aceite, 5 funciones',                   2599.00, 13, 1),
('Sartén antiadherente 28 cm',      'Sartén de aluminio con recubrimiento antiadherente',          349.00, 45, 1),
('Juego de cuchillos de cocina',    'Set de 6 piezas de acero inoxidable con base',                499.00, 27, 1),
('Ventilador de torre 42"',         'Ventilador con control remoto y 3 velocidades',              1299.00, 10, 1),
-- Papelería y oficina
('Cuaderno profesional 100 hojas',  'Cuaderno cuadro grande, pasta dura',                           45.00,200, 1),
('Paquete de bolígrafos BIC x12',   'Bolígrafos tinta azul punto medio',                            89.00,150, 1),
('Resma de papel bond 500 hojas',   'Papel tamaño carta 75 g/m²',                                  129.00, 90, 1),
('Silla ergonómica de oficina',     'Silla con soporte lumbar y descansabrazos ajustables',       2799.00,  7, 1),
('Escritorio para computadora',     'Escritorio de madera 120 x 60 cm',                           1999.00,  6, 1),
('Lámpara LED de escritorio',       'Lámpara flexible con 3 tonos de luz y USB',                   399.00, 31, 1),
-- Deportes y ropa
('Balón de fútbol Nike No. 5',      'Balón de entrenamiento, costura reforzada',                   549.00, 40, 1),
('Tenis Adidas Runfalcon',          'Tenis para correr, suela ligera, talla 26-29 MX',            1299.00, 23, 1),
('Playera deportiva Under Armour',  'Playera de secado rápido, tallas S a XL',                     599.00, 55, 1),
('Mancuernas ajustables 20 kg',     'Par de mancuernas con discos intercambiables',                999.00, 12, 1),
('Tapete de yoga 6 mm',             'Tapete antiderrapante con correa de transporte',              299.00, 48, 1),
('Bicicleta de montaña rodada 26',  'Bicicleta con 21 velocidades y frenos de disco',             5999.00,  4, 1),
-- Videojuegos y entretenimiento
('Control inalámbrico Xbox',        'Control Bluetooth compatible con PC y consola',              1399.00, 19, 1),
('Consola Nintendo Switch Lite',    'Consola portátil color turquesa',                            4999.00,  6, 1),
('Videojuego FC 25 para PS5',       'Simulador de fútbol, edición estándar',                      1199.00, 14, 1),
('Smart TV Hisense 43" 4K',         'Televisión 4K UHD con Google TV',                            6499.00,  8, 1),
-- Casos de prueba: sin stock
('Tarjeta de video RTX 4060',       'Tarjeta gráfica 8 GB GDDR6',                                 8999.00,  0, 1),
('Audífonos Apple AirPods Pro',     'Audífonos con cancelación activa de ruido',                  4799.00,  0, 1),
-- Casos de prueba: productos retirados del catálogo (active = 0)
('Laptop Acer Aspire 3 (descontinuada)', 'Modelo anterior, ya fuera de catálogo',                 9999.00,  2, 0),
('Teclado inalámbrico genérico (retirado)', 'Producto retirado por baja demanda',                  199.00,  5, 0),
('Cámara de seguridad Wi-Fi (retirada)',    'Producto retirado por falla de proveedor',            649.00,  3, 0);

-- -------------------------------------------------------------------------
-- VERIFICACIÓN RÁPIDA
-- -------------------------------------------------------------------------
-- SELECT COUNT(*) AS total_usuarios  FROM Users;      -- debe dar 10
-- SELECT COUNT(*) AS total_productos FROM Products;   -- debe dar 50
-- SELECT u.id, u.full_name, u.email, r.role_name
--   FROM Users u JOIN Roles r ON r.id = u.role_id;
-- SELECT * FROM Products WHERE active = 0;            -- 3 productos retirados
