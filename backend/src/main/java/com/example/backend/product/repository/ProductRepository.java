package com.example.backend.product.repository;

import com.example.backend.category.entity.Category;
import com.example.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    boolean existsByNameIgnoreCase(String name);

    // check if their is a product in the category (for category deletion)
    boolean existsByCategory(Category category);

}
