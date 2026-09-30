-- -------------------------------------------------------------------------
-- 03_insert_data.sql
-- Datos de prueba y pruebas rápidas
-- Ejecutar después de 01_create_tables.sql y 02_constraints.sql
-- -------------------------------------------------------------------------
USE Products_MS;

-- Roles de prueba (solo si no existen)
INSERT INTO Roles (role_name)
SELECT 'Administrador' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'Administrador');
INSERT INTO Roles (role_name)
SELECT 'Cliente' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'Cliente');

-- -------------------------------------------------------------------------
-- PRUEBAS RÁPIDAS (opcional, ejecutar una por una)
-- -------------------------------------------------------------------------
-- CALL sp_RegistrarProducto(1, 'Laptop', 'Laptop 15 pulgadas', 15999.99, 10);
-- CALL sp_EliminarProducto(2, 1);     -- Error: no es administrador
-- CALL sp_EliminarProducto(1, 999);   -- Error: no existe
-- CALL sp_EliminarProducto(1, 1);     -- OK: producto retirado
-- CALL sp_EliminarProducto(1, 1);     -- Error: ya había sido retirado
-- CALL sp_ConsultarProductoPorID(1);  -- Se ve con active = 0
