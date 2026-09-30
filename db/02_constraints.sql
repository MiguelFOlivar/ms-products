-- -------------------------------------------------------------------------
-- 02_constraints.sql
-- Reglas de negocio y validaciones (procedimientos almacenados)
-- Ejecutar después de 01_create_tables.sql
-- -------------------------------------------------------------------------
USE Products_MS;

DELIMITER $$

-- -------------------------------------------------------------------------
-- HU-07: Registrar Producto
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_RegistrarProducto$$
CREATE PROCEDURE sp_RegistrarProducto(
    IN p_user_role_id INT,
    IN p_name VARCHAR(150),
    IN p_description TEXT,
    IN p_price DECIMAL(10,2),
    IN p_stock INT
)
BEGIN
    IF p_user_role_id IS NULL OR p_user_role_id <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El nombre del producto es obligatorio.';
    END IF;

    IF p_price IS NULL OR p_price <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El precio debe ser mayor a cero.';
    END IF;

    IF p_stock IS NULL OR p_stock < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El stock no puede ser un número negativo.';
    END IF;

    INSERT INTO Products (name, description, price, stock, active)
    VALUES (p_name, p_description, p_price, p_stock, 1);

    SELECT LAST_INSERT_ID() AS registered_product_id,
           'Producto registrado con éxito.' AS message;
END$$

-- -------------------------------------------------------------------------
-- HU-08: Consultar producto por ID (API: GET /api/products/{id})
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_ConsultarProductoPorID$$
CREATE PROCEDURE sp_ConsultarProductoPorID(
    IN p_id INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Products WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto solicitado no existe.';
    END IF;

    SELECT id, name, description, price, stock, active, created_at
    FROM Products
    WHERE id = p_id;
END$$

-- -------------------------------------------------------------------------
-- HU-09: Actualizar producto (API: PUT /api/products/{id})
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_ActualizarProducto$$
CREATE PROCEDURE sp_ActualizarProducto(
    IN p_user_role_id INT,
    IN p_id INT,
    IN p_name VARCHAR(150),
    IN p_description TEXT,
    IN p_price DECIMAL(10,2),
    IN p_stock INT
)
BEGIN
    IF p_user_role_id IS NULL OR p_user_role_id <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM Products WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto a modificar no existe.';
    END IF;

    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El nombre del producto es obligatorio.';
    END IF;

    IF p_price IS NULL OR p_price <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El precio debe ser mayor a cero.';
    END IF;

    IF p_stock IS NULL OR p_stock < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El stock no puede ser un número negativo.';
    END IF;

    UPDATE Products
    SET name = p_name,
        description = p_description,
        price = p_price,
        stock = p_stock
    WHERE id = p_id;

    SELECT p_id AS updated_product_id,
           'Producto actualizado con éxito.' AS message;
END$$

-- -------------------------------------------------------------------------
-- HU-10: Eliminar producto (API: DELETE /api/products/{id})
-- Eliminación lógica: el producto se marca como inactivo (active = 0) para
-- que ya no pueda adquirirse, conservando el historial (ventas, reportes).
-- -------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_EliminarProducto$$
CREATE PROCEDURE sp_EliminarProducto(
    IN p_user_role_id INT,   -- Criterio: solo usuario autorizado (Administrador = 1)
    IN p_id INT
)
BEGIN
    -- Criterio: Solo un usuario autorizado puede realizar la operación.
    IF p_user_role_id IS NULL OR p_user_role_id <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Solo los usuarios administradores pueden realizar esta operación.';
    END IF;

    -- Criterio: El producto debe existir.
    IF NOT EXISTS (SELECT 1 FROM Products WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: El producto a eliminar no existe.';
    END IF;

    -- Manejo correcto: no retirar dos veces el mismo producto.
    IF EXISTS (SELECT 1 FROM Products WHERE id = p_id AND active = 0) THEN
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

        UPDATE Products
        SET active = 0
        WHERE id = p_id;

        COMMIT;

        -- Criterio: Debe informar el resultado de la operación.
        SELECT p_id AS deleted_product_id,
               'Producto retirado del catálogo con éxito.' AS message;
    END;
END$$

DELIMITER ;
