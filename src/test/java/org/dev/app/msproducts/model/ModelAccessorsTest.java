package org.dev.app.msproducts.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelAccessorsTest {

    @Test
    void categoryExposesFields() {
        Category category = new Category();
        category.setId(3);
        category.setName("Electrónica");
        category.setActive(true);

        assertEquals(3, category.getId());
        assertEquals("Electrónica", category.getName());
        assertTrue(category.getActive());
    }

    @Test
    void productExposesFieldsAndConstructorInitializesSuppliedValues() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 19, 10, 0);
        Product product = new Product(7, "Producto", "Descripción",
                new BigDecimal("12.50"), 101, true, createdAt, 3);
        product.setId(8);
        product.setName("Actualizado");
        product.setDescription("Nueva descripción");
        product.setPrice(new BigDecimal("15.00"));
        product.setStock(102);
        product.setActive(false);
        product.setCreatedAt(createdAt);
        Category category = new Category();
        category.setId(3);
        product.setCategory(category);

        assertEquals(8, product.getId());
        assertEquals("Actualizado", product.getName());
        assertEquals("Nueva descripción", product.getDescription());
        assertEquals(new BigDecimal("15.00"), product.getPrice());
        assertEquals(102, product.getStock());
        assertFalse(product.getActive());
        assertEquals(createdAt, product.getCreatedAt());
        assertSame(category, product.getCategory());
        assertTrue(new Product().getActive());
    }

    @Test
    void priceHistoryInitializesTimestampAndPrePersistFillsMissingTimestamp() {
        Product product = new Product();
        PriceHistory history = new PriceHistory(
                product, new BigDecimal("10.00"), new BigDecimal("12.00"), "admin");
        assertNotNull(history.getChangedAt());
        assertSame(product, history.getProduct());
        assertEquals(new BigDecimal("10.00"), history.getOldPrice());
        assertEquals(new BigDecimal("12.00"), history.getNewPrice());
        assertEquals("admin", history.getChangedBy());

        history.setId(5L);
        history.setProduct(product);
        history.setOldPrice(new BigDecimal("11.00"));
        history.setNewPrice(new BigDecimal("13.00"));
        history.setChangedBy("operator");
        history.setChangedAt(null);
        history.prePersist();
        assertEquals(5L, history.getId());
        assertSame(product, history.getProduct());
        assertEquals(new BigDecimal("11.00"), history.getOldPrice());
        assertEquals(new BigDecimal("13.00"), history.getNewPrice());
        assertEquals("operator", history.getChangedBy());
        assertNotNull(history.getChangedAt());

        LocalDateTime changedAt = LocalDateTime.of(2026, 10, 19, 10, 0);
        history.setChangedAt(changedAt);
        history.prePersist();
        assertEquals(changedAt, history.getChangedAt());
    }

    @Test
    void stockMovementExposesFieldsAndResolvesAdminActorLabel() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 19, 10, 0);
        StockMovement movement = new StockMovement(7, "IN", 4, "Reposición", 1L, createdAt);
        movement.setCreatedAtIfMissing();
        assertNull(movement.getId());
        assertEquals(7, movement.getProductId());
        assertEquals("IN", movement.getMovementType());
        assertEquals(4, movement.getQuantity());
        assertEquals("Reposición", movement.getReason());
        assertEquals(1L, movement.getActorId());
        assertEquals(createdAt, movement.getCreatedAt());

        StockMovement missingTimestamp = new StockMovement(7, "IN", 4, "Reposición", 1L, null);
        missingTimestamp.setCreatedAtIfMissing();
        assertNotNull(missingTimestamp.getCreatedAt());
    }

    @Test
    void promotionExposesFieldsAndInitializesCreationTime() {
        Promotion promotion = new Promotion();
        assertNotNull(promotion.getProducts());
        promotion.setId(1L);
        promotion.setName("Promoción");
        promotion.setDiscountPercentage(new BigDecimal("10.00"));
        promotion.setStartsAt(LocalDateTime.of(2026, 10, 19, 10, 0));
        promotion.setEndsAt(LocalDateTime.of(2026, 10, 20, 10, 0));
        promotion.setActive(false);
        Set<Product> products = Set.of(new Product());
        promotion.setProducts(products);
        promotion.setCreatedAt(null);
        promotion.setCreatedAt();

        assertEquals(1L, promotion.getId());
        assertEquals("Promoción", promotion.getName());
        assertEquals(new BigDecimal("10.00"), promotion.getDiscountPercentage());
        assertEquals(LocalDateTime.of(2026, 10, 19, 10, 0), promotion.getStartsAt());
        assertEquals(LocalDateTime.of(2026, 10, 20, 10, 0), promotion.getEndsAt());
        assertFalse(promotion.getActive());
        assertEquals(products, promotion.getProducts());
        assertNotNull(promotion.getCreatedAt());
        LocalDateTime createdAt = promotion.getCreatedAt();
        promotion.setCreatedAt();
        assertEquals(createdAt, promotion.getCreatedAt());
    }
}
