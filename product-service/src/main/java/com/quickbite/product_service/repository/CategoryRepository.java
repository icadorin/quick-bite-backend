package com.quickbite.product_service.repository;

import com.quickbite.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends
    JpaRepository<Category, Long>,
    JpaSpecificationExecutor<Category> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}