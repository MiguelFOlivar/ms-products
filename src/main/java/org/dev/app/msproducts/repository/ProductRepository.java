package org.dev.app.msproducts.repository;

import org.dev.app.msproducts.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Product.
 * Proporciona operaciones CRUD y permite definir queries personalizadas.
 */

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Aquí agregaremos queries personalizadas si se requieren
    // Ejemplo: List<Product> findByNameContaining(String name);

    List<Product> findByActiveTrue();
    List<Product> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Product> findByActiveFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Integer id);
}
