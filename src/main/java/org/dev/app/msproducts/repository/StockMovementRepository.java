package org.dev.app.msproducts.repository;

import org.dev.app.msproducts.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByCreatedAtDescIdDesc(Integer productId);
}
