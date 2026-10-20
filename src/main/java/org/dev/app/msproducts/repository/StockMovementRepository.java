package org.dev.app.msproducts.repository;

import org.dev.app.msproducts.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query(value = """
            SELECT sm.id AS id,
                   sm.product_id AS productId,
                   sm.quantity AS quantity,
                   sm.movement_type AS movementType,
                   sm.reason AS reason,
                   sm.actor_id AS actorId,
                   CONCAT_WS(' ', u.name, u.paternal_lastname, u.maternal_lastname) AS actor,
                   sm.created_at AS createdAt
            FROM stock_movements sm
            JOIN users u ON u.id_user = sm.actor_id
            WHERE sm.product_id = :productId
            ORDER BY sm.created_at DESC, sm.id DESC
            """, nativeQuery = true)
    List<StockMovementHistoryProjection> findHistoryByProductId(@Param("productId") Integer productId);
}
