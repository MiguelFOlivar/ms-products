package org.dev.app.msproducts.repository;

import org.dev.app.msproducts.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository <Category, Integer> {
}
