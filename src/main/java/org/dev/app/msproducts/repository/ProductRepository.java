package org.dev.app.msproducts.repository;

import org.dev.app.msproducts.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Product.
 * Proporciona operaciones CRUD y permite definir queries personalizadas.
 */

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Aquí agregaremos queries personalizadas si se requieren
    // Ejemplo: List<Product> findByNameContaining(String name);

    List<Product> findByActiveTrue();
}
