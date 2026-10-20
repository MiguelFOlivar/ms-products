package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.StockAdjustmentRequest;
import org.dev.app.msproducts.dto.StockMovementType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StockAdjustmentRollbackIntegrationTest {

    private static final String FAILURE_TRIGGER = "fail_stock_movement_insert_for_rollback_test";
    private static final String FAILURE_MESSAGE = "Controlled stock movement insert failure";
    private static final int INITIAL_STOCK = 150;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer categoryId;
    private Integer productId;
    private Long createdAdminId;

    @BeforeEach
    void setUp() {
        ensureDemoAdminExists();
        createTestCategoryAndProduct();
        createFailureTrigger();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + FAILURE_TRIGGER);
        if (productId != null) {
            jdbcTemplate.update("DELETE FROM stock_movements WHERE product_id = ?", productId);
            jdbcTemplate.update("DELETE FROM products WHERE id = ?", productId);
        }
        if (categoryId != null) {
            jdbcTemplate.update("DELETE FROM categories WHERE id = ?", categoryId);
        }
        if (createdAdminId != null) {
            jdbcTemplate.update("DELETE FROM users WHERE id_user = ?", createdAdminId);
        }
    }

    @Test
    void rollsBackStockUpdateWhenMovementInsertFails() {
        int movementsBefore = movementCount();

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(1);
        request.setMovementType(StockMovementType.IN);
        request.setReason("Integration rollback test");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> stockMovementService.adjustStock(productId, request, "admin@demo.com"));

        assertTrue(hasCauseMessage(exception, FAILURE_MESSAGE),
                "The operation should fail because the test trigger rejects the movement insert");
        assertEquals(INITIAL_STOCK, currentStock(),
                "The product stock must be rolled back when movement persistence fails");
        assertEquals(movementsBefore, movementCount(),
                "A failed adjustment must not leave a movement in history");
    }

    private void ensureDemoAdminExists() {
        var adminIds = jdbcTemplate.query(
                """
                SELECT id_user
                FROM users
                WHERE LOWER(email) = LOWER(?)
                  AND UPPER(role) = 'ADMIN'
                  AND is_active = TRUE
                ORDER BY id_user
                LIMIT 1
                """,
                (resultSet, rowNum) -> resultSet.getLong("id_user"),
                "admin@demo.com");

        if (!adminIds.isEmpty()) {
            return;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO users
                        (name, paternal_lastname, maternal_lastname, email, password, role, is_active)
                    VALUES (?, ?, ?, ?, ?, 'ADMIN', TRUE)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "Rollback Test Admin");
            statement.setString(2, "Test");
            statement.setString(3, "Test");
            statement.setString(4, "admin@demo.com");
            statement.setString(5, "not-used-by-this-test");
            return statement;
        }, keyHolder);
        createdAdminId = keyHolder.getKey().longValue();
    }

    private void createTestCategoryAndProduct() {
        String uniqueName = "rollback-test-" + UUID.randomUUID();
        KeyHolder categoryKey = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO categories (name, active) VALUES (?, TRUE)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, uniqueName);
            return statement;
        }, categoryKey);
        categoryId = categoryKey.getKey().intValue();

        KeyHolder productKey = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO products
                        (name, description, price, stock, active, created_at, category_id)
                    VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, uniqueName);
            statement.setString(2, "Temporary product for rollback integration test");
            statement.setBigDecimal(3, java.math.BigDecimal.ONE);
            statement.setInt(4, INITIAL_STOCK);
            statement.setInt(5, categoryId);
            return statement;
        }, productKey);
        productId = productKey.getKey().intValue();
    }

    private void createFailureTrigger() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + FAILURE_TRIGGER);
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_stock_movement_insert_for_rollback_test
                BEFORE INSERT ON stock_movements
                FOR EACH ROW
                BEGIN
                    IF NEW.product_id = %d THEN
                        SIGNAL SQLSTATE '45000'
                            SET MESSAGE_TEXT = '%s';
                    END IF;
                END
                """.formatted(productId, FAILURE_MESSAGE));
    }

    private int currentStock() {
        return jdbcTemplate.queryForObject(
                "SELECT stock FROM products WHERE id = ?",
                Integer.class,
                productId);
    }

    private int movementCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_movements WHERE product_id = ?",
                Integer.class,
                productId);
    }

    private boolean hasCauseMessage(Throwable throwable, String expectedMessage) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(expectedMessage)) {
                return true;
            }
        }
        return false;
    }
}
