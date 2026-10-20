package org.dev.app.msproducts.repository;

import java.time.LocalDateTime;

public interface StockMovementHistoryProjection {

    Long getId();

    Integer getProductId();

    Integer getQuantity();

    String getMovementType();

    String getReason();

    Long getActorId();

    String getActor();

    LocalDateTime getCreatedAt();
}
