package org.dev.app.msproducts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockMovementResponse {

    private final Long id;
    private final Integer productId;
    private final Integer quantity;
    private final String movementType;
    private final String reason;
    private final Long actorId;
    private final String actor;
    private final LocalDateTime createdAt;
    private final Integer stockAfter;

    public StockMovementResponse(Long id, Integer productId, Integer quantity,
                                 String movementType, String reason, Long actorId, String actor,
                                 LocalDateTime createdAt, Integer stockAfter) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.movementType = movementType;
        this.reason = reason;
        this.actorId = actorId;
        this.actor = actor;
        this.createdAt = createdAt;
        this.stockAfter = stockAfter;
    }

    public Long getId() {
        return id;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getMovementType() {
        return movementType;
    }

    public String getReason() {
        return reason;
    }

    public String getActor() {
        return actor;
    }

    public Long getActorId() {
        return actorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }
}
