
INSERT INTO products (name, description, price, stock, active) VALUES
-- Electrónica y Tecnología
('Smartphone Galaxy X1', 'Teléfono inteligente con 128GB de almacenamiento y cámara de 64MP.', 599.99, 50, 1),
('Laptop Pro 15', 'Computadora portátil con procesador de última generación y 16GB RAM.', 1249.50, 20, 1),
('Auriculares Inalámbricos', 'Auriculares con cancelación de ruido activa y batería de 30 horas.', 89.99, 120, 1),
('Monitor Gamer 27"', 'Monitor curvo con frecuencia de actualización de 144Hz y resolución QHD.', 279.00, 15, 1),
('Teclado Mecánico RGB', 'Teclado para juegos con switches rojos y retroiluminación personalizable.', 65.00, 45, 1),
('Ratón Óptico Ergonómico', 'Ratón inalámbrico recargable con diseño para reducir la fatiga.', 35.50, 80, 1),
('Cargador Rápido GaN 65W', 'Cargador de pared compacto con dos puertos USB-C y un puerto USB-A.', 24.99, 200, 1),
('Tablet Entretenimiento 10"', 'Pantalla Full HD, ideal para streaming de video y lectura.', 180.00, 35, 1),

-- Hogar y Cocina
('Cafetera de Goteo', 'Cafetera programable con jarra de vidrio para 12 tazas.', 45.90, 60, 1),
('Freidora de Aire 4L', 'Cocina saludable sin aceite con panel digital táctil.', 99.00, 40, 1),
('Aspiradora Robot', 'Navegación inteligente, regresa automáticamente a su base de carga.', 210.00, 12, 1),
('Licuadora de Alta Potencia', 'Cuchillas de acero inoxidable capaces de triturar hielo fácilmente.', 55.00, 75, 1),
('Juego de Sartenes (3 piezas)', 'Aluminio antiadherente compatible con todo tipo de estufas.', 39.99, 90, 1),
('Lámpara de Escritorio LED', 'Brazo flexible y regulación de brillo y temperatura de color.', 19.95, 110, 1),

-- Ropa y Accesorios
('Camiseta Algodón Básica', 'Camiseta de cuello redondo 100% algodón, color negro.', 15.00, 150, 1),
('Sudadera con Capucha', 'Sudadera deportiva unisex con bolsillo canguro frontal.', 34.99, 85, 1),
('Pantalón Vaquero Slim Fit', 'Jeans de mezclilla elástica para mayor comodidad.', 45.00, 65, 1),
('Tenis Deportivos Running', 'Calzado ligero con amortiguación optimizada para correr.', 79.90, 40, 1),
('Mochila Impermeable', 'Compartimento acolchado para laptop de hasta 15.6 pulgadas.', 29.99, 100, 1),
('Reloj de Pulsera Clásico', 'Reloj analógico con correa de cuero marrón y caja de acero.', 115.00, 25, 1),

-- Deportes y Cuidado Personal
('Botella de Agua Térmica', 'Mantiene las bebidas frías por 24 horas o calientes por 12.', 18.50, 250, 1),
('Tapete de Yoga Antideslizante', 'Grosor de 6mm para una excelente amortiguación y soporte.', 22.00, 70, 1),
('Set de Mancuernas 10kg', 'Par de mancuernas con revestimiento de neopreno para mejor agarre.', 32.00, 30, 1),
('Crema Hidratante Facial', 'Fórmula ligera de rápida absorción con ácido hialurónico.', 14.99, 140, 1),
('Secador de Pelo Iónico', 'Motor profesional de alta velocidad que reduce el frizz.', 48.00, 55, 1),

-- Productos Descontinuados, Agotados o Especiales
('Consola Retro Classic', 'Edición limitada con 50 juegos clásicos preinstalados.', 89.99, 0, 1), -- Producto sin existencias
('Kit Limpieza Lentes (Antiguo)', 'Modelo anterior del líquido limpiador de pantallas.', 5.00, 10, 0), -- Producto inactivo
('Filtro de Agua Repuesto', 'Filtro compatible con jarras de la generación pasada.', 12.00, 0, 0), -- Inactivo y sin stock
('Suscripción Digital Premium', 'Acceso mensual a la biblioteca completa de cursos.', 9.99, 9999, 1), -- Producto digital (alto stock)
('Muestra Gratis Perfume', 'Pequeña botella de prueba de la nueva fragancia de la temporada.', 0.00, 500, 1); -- Producto de cortesía
