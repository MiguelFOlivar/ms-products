package org.dev.app.msproducts.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoAccessorsTest {

    @Test
    void productRegisterResponseExposesAllFields() {
        ProductRegisterResponse response = new ProductRegisterResponse();
        response.setName("Producto");
        response.setDescription("Descripción");
        response.setPrice(new BigDecimal("12.50"));
        response.setStock(101);
        response.setActive(1);
        response.setDate("2026-10-19");
        response.setId(7);
        response.setCategoryId(3);

        assertEquals("Producto", response.getName());
        assertEquals("Descripción", response.getDescription());
        assertEquals(new BigDecimal("12.50"), response.getPrice());
        assertEquals(101, response.getStock());
        assertEquals(1, response.getActive());
        assertEquals("2026-10-19", response.getDate());
        assertEquals(7, response.getId());
        assertEquals(3, response.getCategoryId());
    }

    @Test
    void requestAndResponseDtosExposeTheirFields() {
        ProductRequestDTO request = new ProductRequestDTO(
                "Producto inicial", "Descripción inicial", new BigDecimal("10.00"), 100);
        assertEquals("Producto inicial", request.getName());
        assertEquals("Descripción inicial", request.getDescription());
        assertEquals(new BigDecimal("10.00"), request.getPrice());
        assertEquals(100, request.getStock());
        request.setName("Producto");
        request.setDescription("Descripción");
        request.setPrice(new BigDecimal("12.50"));
        request.setStock(101);
        request.setCategoryId(3);
        assertEquals("Producto", request.getName());
        assertEquals("Descripción", request.getDescription());
        assertEquals(new BigDecimal("12.50"), request.getPrice());
        assertEquals(101, request.getStock());
        assertEquals(3, request.getCategoryId());

        ProductResponseDTO response = new ProductResponseDTO(
                7, "Producto", "Descripción", new BigDecimal("12.50"), 101, true, 3);
        response.setId(8);
        response.setName("Otro");
        response.setDescription("Otra descripción");
        response.setPrice(new BigDecimal("15.00"));
        response.setStock(102);
        response.setActive(false);
        response.setCategoryId(4);
        assertEquals(8, response.getId());
        assertEquals("Otro", response.getName());
        assertEquals("Otra descripción", response.getDescription());
        assertEquals(new BigDecimal("15.00"), response.getPrice());
        assertEquals(102, response.getStock());
        assertEquals(false, response.getActive());
        assertEquals(4, response.getCategoryId());
    }

    @Test
    void registerRequestExposesAllFields() {
        ProductRegisterRequest request = new ProductRegisterRequest();
        request.setName("Producto");
        request.setDescription("Descripción");
        request.setPrice(new BigDecimal("12.50"));
        request.setStock(101);
        request.setActive(1);
        request.setDate("2026-10-19");
        request.setCategoryId(3);
        request.setId(7);
        request.setQuantity(2);

        assertEquals("Producto", request.getName());
        assertEquals("Descripción", request.getDescription());
        assertEquals(new BigDecimal("12.50"), request.getPrice());
        assertEquals(101, request.getStock());
        assertEquals(1, request.getActive());
        assertEquals("2026-10-19", request.getDate());
        assertEquals(3, request.getCategoryId());
        assertEquals(7, request.getId());
        assertEquals(2, request.getQuantity());
    }

    @Test
    void bulkPriceDtosExposeConstructedAndUpdatedValues() {
        PriceUpdateItem emptyUpdate = new PriceUpdateItem();
        assertEquals(null, emptyUpdate.getProductId());
        assertEquals(null, emptyUpdate.getNewPrice());
        PriceUpdateItem update = new PriceUpdateItem(7, new BigDecimal("15.00"));
        assertEquals(7, update.getProductId());
        assertEquals(new BigDecimal("15.00"), update.getNewPrice());
        update.setProductId(8);
        update.setNewPrice(new BigDecimal("16.00"));
        assertEquals(8, update.getProductId());
        assertEquals(new BigDecimal("16.00"), update.getNewPrice());

        ProductPriceUpdatedDTO emptyUpdated = new ProductPriceUpdatedDTO();
        assertEquals(null, emptyUpdated.getProductId());
        ProductPriceUpdatedDTO updated = new ProductPriceUpdatedDTO(
                7, "Producto", new BigDecimal("12.50"), new BigDecimal("15.00"));
        assertEquals(7, updated.getProductId());
        assertEquals("Producto", updated.getName());
        assertEquals(new BigDecimal("12.50"), updated.getOldPrice());
        assertEquals(new BigDecimal("15.00"), updated.getNewPrice());
        updated.setProductId(8);
        updated.setName("Actualizado");
        updated.setOldPrice(new BigDecimal("13.00"));
        updated.setNewPrice(new BigDecimal("16.00"));
        assertEquals(8, updated.getProductId());
        assertEquals("Actualizado", updated.getName());
        assertEquals(new BigDecimal("13.00"), updated.getOldPrice());
        assertEquals(new BigDecimal("16.00"), updated.getNewPrice());

        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(update));
        assertEquals(List.of(update), request.getUpdates());
        request.setUpdates(List.of());
        assertEquals(List.of(), request.getUpdates());

        BulkPriceUpdateResponse emptyResponse = new BulkPriceUpdateResponse();
        assertEquals(0, emptyResponse.getTotalUpdated());
        assertEquals(null, emptyResponse.getMessage());
        BulkPriceUpdateResponse response = new BulkPriceUpdateResponse("Actualizado", 1, List.of(updated));
        assertEquals("Actualizado", response.getMessage());
        assertEquals(1, response.getTotalUpdated());
        assertEquals(List.of(updated), response.getUpdatedProducts());
        response.setMessage("Completado");
        response.setTotalUpdated(2);
        response.setUpdatedProducts(List.of());
        assertEquals("Completado", response.getMessage());
        assertEquals(2, response.getTotalUpdated());
        assertEquals(List.of(), response.getUpdatedProducts());
    }

    @Test
    void paginationRequestExposesAllFields() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        request.setName("Producto");
        request.setCategoryId(3);
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("20.00"));
        request.setPage(2);
        request.setSize(5);

        assertEquals("Producto", request.getName());
        assertEquals(3, request.getCategoryId());
        assertEquals(new BigDecimal("10.00"), request.getMinPrice());
        assertEquals(new BigDecimal("20.00"), request.getMaxPrice());
        assertEquals(2, request.getPage());
        assertEquals(5, request.getSize());
    }

    @Test
    void promotionAndStockDtosExposeTheirFields() {
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 19, 10, 0);
        LocalDateTime endsAt = startsAt.plusDays(1);
        PromotionProductDTO product = new PromotionProductDTO(
                7, "Producto", new BigDecimal("20.00"), new BigDecimal("18.00"));
        assertEquals(7, product.getProductId());
        assertEquals("Producto", product.getName());
        assertEquals(new BigDecimal("20.00"), product.getOriginalPrice());
        assertEquals(new BigDecimal("18.00"), product.getFinalPrice());

        PromotionResponse promotion = new PromotionResponse(
                1L, "Promoción", new BigDecimal("10.00"), startsAt, endsAt, true, List.of(product));
        assertEquals(1L, promotion.getId());
        assertEquals("Promoción", promotion.getName());
        assertEquals(new BigDecimal("10.00"), promotion.getDiscountPercentage());
        assertEquals(startsAt, promotion.getStartsAt());
        assertEquals(endsAt, promotion.getEndsAt());
        assertEquals(true, promotion.getActive());
        assertEquals(List.of(product), promotion.getProducts());

        StockAdjustmentRequest adjustment = new StockAdjustmentRequest();
        adjustment.setQuantity(4);
        adjustment.setMovementType(StockMovementType.IN);
        adjustment.setReason("Reposición");
        assertEquals(4, adjustment.getQuantity());
        assertEquals(StockMovementType.IN, adjustment.getMovementType());
        assertEquals("Reposición", adjustment.getReason());

        StockMovementResponse movement = new StockMovementResponse(
                9L, 7, 4, "IN", "Reposición", 10L, "Admin Demo", startsAt, 104);
        assertEquals(9L, movement.getId());
        assertEquals(7, movement.getProductId());
        assertEquals(4, movement.getQuantity());
        assertEquals("IN", movement.getMovementType());
        assertEquals("Reposición", movement.getReason());
        assertEquals(10L, movement.getActorId());
        assertEquals("Admin Demo", movement.getActor());
        assertEquals(startsAt, movement.getCreatedAt());
        assertEquals(104, movement.getStockAfter());
    }
}
