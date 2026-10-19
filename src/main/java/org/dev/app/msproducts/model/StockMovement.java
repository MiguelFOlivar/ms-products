package org.dev.app.msproducts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "movement_type", nullable = false, length = 20, updatable = false)
    private String movementType;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    @Column(nullable = false, length = 255, updatable = false)
    private String reason;

    @Column(name = "actor_id", nullable = false, updatable = false)
    private Long actorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StockMovement() {
    }

    public StockMovement(Integer productId, String movementType, Integer quantity,
                         String reason, Long actorId, LocalDateTime createdAt) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reason = reason;
        this.actorId = actorId;
        this.createdAt = createdAt;
    }

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public Long getActorId() {
        return actorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
