package org.dev.app.msproducts.repository;

import jakarta.persistence.LockModeType;
import org.dev.app.msproducts.model.Promotion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select promotion from Promotion promotion where promotion.id = :id")
    Optional<Promotion> findLockedById(@Param("id") Long id);

    @EntityGraph(attributePaths = "products")
    @Query("""
            select distinct promotion from Promotion promotion
            where promotion.active = true
              and promotion.startsAt <= :now
              and promotion.endsAt > :now
            """)
    List<Promotion> findCurrentlyActive(@Param("now") LocalDateTime now);

    @Query("""
            select distinct promotion from Promotion promotion
            join promotion.products product
            where promotion.active = true
              and product.id in :productIds
              and promotion.startsAt < :endsAt
              and promotion.endsAt > :startsAt
              and (:excludedId is null or promotion.id <> :excludedId)
            """)
    List<Promotion> findOverlappingPromotions(
            @Param("productIds") Collection<Integer> productIds,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt,
            @Param("excludedId") Long excludedId);
}
