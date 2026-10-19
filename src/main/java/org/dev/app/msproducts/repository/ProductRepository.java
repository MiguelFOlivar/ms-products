package org.dev.app.msproducts.repository;

import jakarta.persistence.LockModeType;
import org.dev.app.msproducts.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Product.
 * Proporciona operaciones CRUD y permite definir queries personalizadas.
 */
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByActiveTrue();
    List<Product> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Product> findByActiveFalse();

    @Query("""
    SELECT p FROM Product p
    WHERE p.active = true
      AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<Product> searchCatalog(@Param("name") String name,
                                @Param("categoryId") Integer categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Integer id);
}