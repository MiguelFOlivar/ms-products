USE ecommerce;

-- 1. Categorías
INSERT IGNORE INTO categories (name, active) VALUES
('Electrónica y Tecnología', 1),
('Hogar y Cocina', 1),
('Ropa y Accesorios', 1),
('Deportes y Cuidado Personal', 1),
('Especiales', 1);

-- 2. Guardar los ids en variables
SET @cat_electronica = (SELECT id FROM categories WHERE name = 'Electrónica y Tecnología');
SET @cat_hogar       = (SELECT id FROM categories WHERE name = 'Hogar y Cocina');
SET @cat_ropa        = (SELECT id FROM categories WHERE name = 'Ropa y Accesorios');
SET @cat_deportes    = (SELECT id FROM categories WHERE name = 'Deportes y Cuidado Personal');
SET @cat_especiales  = (SELECT id FROM categories WHERE name = 'Especiales');

-- 3. Productos
INSERT INTO products (name, description, price, stock, active, category_id) VALUES
-- Electrónica y Tecnología
('Smartphone Galaxy X1', 'Teléfono inteligente con 128GB de almacenamiento y cámara de 64MP.', 599.99, 50, 1, @cat_electronica),
('Laptop Pro 15', 'Computadora portátil con procesador de última generación y 16GB RAM.', 1249.50, 20, 1, @cat_electronica),
('Auriculares Inalámbricos', 'Auriculares con cancelación de ruido activa y batería de 30 horas.', 89.99, 120, 1, @cat_electronica),
('Monitor Gamer 27"', 'Monitor curvo con frecuencia de actualización de 144Hz y resolución QHD.', 279.00, 15, 1, @cat_electronica),
('Teclado Mecánico RGB', 'Teclado para juegos con switches rojos y retroiluminación personalizable.', 65.00, 45, 1, @cat_electronica),
('Ratón Óptico Ergonómico', 'Ratón inalámbrico recargable con diseño para reducir la fatiga.', 35.50, 80, 1, @cat_electronica),
('Cargador Rápido GaN 65W', 'Cargador de pared compacto con dos puertos USB-C y un puerto USB-A.', 24.99, 200, 1, @cat_electronica),
('Tablet Entretenimiento 10"', 'Pantalla Full HD, ideal para streaming de video y lectura.', 180.00, 35, 1, @cat_electronica),

-- Hogar y Cocina
('Cafetera de Goteo', 'Cafetera programable con jarra de vidrio para 12 tazas.', 45.90, 60, 1, @cat_hogar),
('Freidora de Aire 4L', 'Cocina saludable sin aceite con panel digital táctil.', 99.00, 40, 1, @cat_hogar),
('Aspiradora Robot', 'Navegación inteligente, regresa automáticamente a su base de carga.', 210.00, 12, 1, @cat_hogar),
('Licuadora de Alta Potencia', 'Cuchillas de acero inoxidable capaces de triturar hielo fácilmente.', 55.00, 75, 1, @cat_hogar),
('Juego de Sartenes (3 piezas)', 'Aluminio antiadherente compatible con todo tipo de estufas.', 39.99, 90, 1, @cat_hogar),
('Lámpara de Escritorio LED', 'Brazo flexible y regulación de brillo y temperatura de color.', 19.95, 110, 1, @cat_hogar),

-- Ropa y Accesorios
('Camiseta Algodón Básica', 'Camiseta de cuello redondo 100% algodón, color negro.', 15.00, 150, 1, @cat_ropa),
('Sudadera con Capucha', 'Sudadera deportiva unisex con bolsillo canguro frontal.', 34.99, 85, 1, @cat_ropa),
('Pantalón Vaquero Slim Fit', 'Jeans de mezclilla elástica para mayor comodidad.', 45.00, 65, 1, @cat_ropa),
('Tenis Deportivos Running', 'Calzado ligero con amortiguación optimizada para correr.', 79.90, 40, 1, @cat_ropa),
('Mochila Impermeable', 'Compartimento acolchado para laptop de hasta 15.6 pulgadas.', 29.99, 100, 1, @cat_ropa),
('Reloj de Pulsera Clásico', 'Reloj analógico con correa de cuero marrón y caja de acero.', 115.00, 25, 1, @cat_ropa),

-- Deportes y Cuidado Personal
('Botella de Agua Térmica', 'Mantiene las bebidas frías por 24 horas o calientes por 12.', 18.50, 250, 1, @cat_deportes),
('Tapete de Yoga Antideslizante', 'Grosor de 6mm para una excelente amortiguación y soporte.', 22.00, 70, 1, @cat_deportes),
('Set de Mancuernas 10kg', 'Par de mancuernas con revestimiento de neopreno para mejor agarre.', 32.00, 30, 1, @cat_deportes),
('Crema Hidratante Facial', 'Fórmula ligera de rápida absorción con ácido hialurónico.', 14.99, 140, 1, @cat_deportes),
('Secador de Pelo Iónico', 'Motor profesional de alta velocidad que reduce el frizz.', 48.00, 55, 1, @cat_deportes),

-- Descontinuados, agotados o especiales
('Consola Retro Classic', 'Edición limitada con 50 juegos clásicos preinstalados.', 89.99, 0, 1, @cat_especiales),            -- Sin existencias
('Kit Limpieza Lentes (Antiguo)', 'Modelo anterior del líquido limpiador de pantallas.', 5.00, 10, 0, @cat_especiales),          -- Inactivo
('Filtro de Agua Repuesto', 'Filtro compatible con jarras de la generación pasada.', 12.00, 0, 0, @cat_especiales),              -- Inactivo y sin stock
('Suscripción Digital Premium', 'Acceso mensual a la biblioteca completa de cursos.', 9.99, 9999, 1, @cat_especiales),           -- Producto digital
('Muestra Gratis Perfume', 'Pequeña botella de prueba de la nueva fragancia de la temporada.', 0.00, 500, 1, @cat_especiales);  -- Cortesía