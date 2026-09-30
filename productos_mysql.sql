-- -------------------------------------------------------------------------
-- BASE DE DATOS Y TABLAS (MySQL Workbench)
-- -------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS Productos
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE Productos;

-- 1. Tabla de Roles para control de acceso
CREATE TABLE IF NOT EXISTS Roles (
    id_rol INT PRIMARY KEY AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla de Productos
CREATE TABLE IF NOT EXISTS Productos (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,      -- 1 = disponible, 0 = retirado del catálogo
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Roles de prueba (solo si no existen)
INSERT INTO Roles (nombre_rol)
SELECT 'Administrador' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE nombre_rol = 'Administrador');
INSERT INTO Roles (nombre_rol)
SELECT 'Cliente' WHERE NOT EXISTS (SELECT 1 FROM Roles WHERE nombre_rol = 'Cliente');

DELIMITER $$

-- -------------------------------------------------------------------------
-- HU-07: Registrar Producto
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_RegistrarProducto$$
CREATE PROCEDURE sp_RegistrarProducto(
    IN p_id_rol_usuario INT,
    IN p_nombre VARCHAR(150),
    IN p_descripcion TEXT,
    IN p_precio DECIMAL(10,2),
    IN p_stock INT
)
BEGIN
    IF p_id_rol_usuario IS NULL OR p_id_rol_usuario <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El nombre del producto es obligatorio.';
    END IF;

    IF p_precio IS NULL OR p_precio <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El precio debe ser mayor a cero.';
    END IF;

    IF p_stock IS NULL OR p_stock < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El stock no puede ser un número negativo.';
    END IF;

    INSERT INTO Productos (nombre, descripcion, precio, stock, activo)
    VALUES (p_nombre, p_descripcion, p_precio, p_stock, 1);

    SELECT LAST_INSERT_ID() AS id_producto_registrado,
           'Producto registrado con éxito.' AS Mensaje;
END$$

-- -------------------------------------------------------------------------
-- HU-08: Consultar producto por ID (API: GET /api/products/{id})
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_ConsultarProductoPorID$$
CREATE PROCEDURE sp_ConsultarProductoPorID(
    IN p_id_producto INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Productos WHERE id_producto = p_id_producto) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto solicitado no existe.';
    END IF;

    SELECT id_producto, nombre, descripcion, precio, stock, activo, fecha_creacion
    FROM Productos
    WHERE id_producto = p_id_producto;
END$$

-- -------------------------------------------------------------------------
-- HU-09: Actualizar producto (API: PUT /api/products/{id})
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_ActualizarProducto$$
CREATE PROCEDURE sp_ActualizarProducto(
    IN p_id_rol_usuario INT,
    IN p_id_producto INT,
    IN p_nombre VARCHAR(150),
    IN p_descripcion TEXT,
    IN p_precio DECIMAL(10,2),
    IN p_stock INT
)
BEGIN
    IF p_id_rol_usuario IS NULL OR p_id_rol_usuario <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM Productos WHERE id_producto = p_id_producto) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto a modificar no existe.';
    END IF;

    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El nombre del producto es obligatorio.';
    END IF;

    IF p_precio IS NULL OR p_precio <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El precio debe ser mayor a cero.';
    END IF;

    IF p_stock IS NULL OR p_stock < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El stock no puede ser un número negativo.';
    END IF;

    UPDATE Productos
    SET nombre = p_nombre,
        descripcion = p_descripcion,
        precio = p_precio,
        stock = p_stock
    WHERE id_producto = p_id_producto;

    SELECT p_id_producto AS id_producto_actualizado,
           'Producto actualizado con éxito.' AS Mensaje;
END$$

-- -------------------------------------------------------------------------
-- HU-10: Eliminar producto (API: DELETE /api/products/{id})
-- Eliminación lógica: el producto se marca como inactivo (activo = 0) para
-- que ya no pueda adquirirse, conservando el historial (ventas, reportes).
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_EliminarProducto$$
CREATE PROCEDURE sp_EliminarProducto(
    IN p_id_rol_usuario INT,   -- Criterio: solo usuario autorizado (Administrador = 1)
    IN p_id_producto INT
)
BEGIN
    -- Criterio: Solo un usuario autorizado puede realizar la operación.
    IF p_id_rol_usuario IS NULL OR p_id_rol_usuario <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    -- Criterio: El producto debe existir.
    IF NOT EXISTS (SELECT 1 FROM Productos WHERE id_producto = p_id_producto) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto a eliminar no existe.';
    END IF;

    -- Manejo correcto: no retirar dos veces el mismo producto.
    IF EXISTS (SELECT 1 FROM Productos WHERE id_producto = p_id_producto AND activo = 0) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto ya había sido retirado del catálogo.';
    END IF;

    -- Criterio: El sistema debe manejar correctamente la eliminación.
    -- Bloque anidado para que el handler solo atrape errores de la transacción
    -- y no los SIGNAL de validación de arriba.
    BEGIN
        DECLARE v_msg TEXT;
        DECLARE v_msg_final VARCHAR(255);

        DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1 v_msg = MESSAGE_TEXT;
            ROLLBACK;
            SET v_msg_final = LEFT(CONCAT('Error al eliminar el producto: ', v_msg), 128);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_msg_final;
        END;

        START TRANSACTION;

        UPDATE Productos
        SET activo = 0
        WHERE id_producto = p_id_producto;

        COMMIT;

        -- Criterio: Debe informar el resultado de la operación.
        SELECT p_id_producto AS id_producto_eliminado,
               'Producto retirado del catálogo con éxito.' AS Mensaje;
    END;
END$$

DELIMITER ;

-- -------------------------------------------------------------------------
-- PRUEBAS RÁPIDAS (opcional, ejecutar una por una)
-- -------------------------------------------------------------------------
-- CALL sp_RegistrarProducto(1, 'Laptop', 'Laptop 15 pulgadas', 15999.99, 10);
-- CALL sp_EliminarProducto(2, 1);     -- Error: no es administrador
-- CALL sp_EliminarProducto(1, 999);   -- Error: no existe
-- CALL sp_EliminarProducto(1, 1);     -- OK: producto retirado
-- CALL sp_EliminarProducto(1, 1);     -- Error: ya había sido retirado
-- CALL sp_ConsultarProductoPorID(1);  -- Se ve con activo = 0
