package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    private void validateCategoryRequest(CategoryRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new DataValidationException("Name is required");
        }

        if (request.getName().length() > 100) {
            throw new DataValidationException("Category name must not exceed 100 characters");
        }

        if (request.getDescription() != null && request.getDescription().length() > 500) {
            throw new DataValidationException("Category description must not exceed 500 characters");
        }

        if (request.getSortOrder() != null && request.getSortOrder() < 0) {
            throw new DataValidationException("Sort order must be a position number");
        }
    }

    public List<CategoryResponse> getAllCategories() {
        try {
            return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving categories", e);
        }
    }

    public CategoryResponse getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid category ID");
        }

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        validateCategoryRequest(request);

        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException("Category with name '" + trimmedName + "' already exists");
        }

        try {
            Category category = Category.builder()
                .name(trimmedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

            Category savedCategory = categoryRepository.save(category);
            return mapToResponse(savedCategory);

        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error creating category", e);
        }
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        if (id == null || id < 0) {
            throw new DataValidationException("Invalid category ID");
        }

        validateCategoryRequest(request);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();

        if (!category.getName().equals(trimmedName) && categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException("Category with name '" + trimmedName + "' already exists");
        }

        try {
            category.setName(trimmedName);
            category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            category.setImageUrl(request.getImageUrl());
            category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder());

            if (request.getIsActive() != null) {
                category.setIsActive(request.getIsActive());
            }

            Category updatedCategory = categoryRepository.save(category);
            return mapToResponse(updatedCategory);

        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error updating category", e);
        }
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid category ID");
        }

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Long productCount = productRepository.countByRestaurantIdAndIsAvailableTrue(id);
        if (productCount > 0) {
            throw new BusinessRuleViolationException(
                "Cannot delete category with associated products. There are " + productCount
                + "product in this category."
            );
        }
        try {
            category.setIsActive(false);
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error deleting category", e);
        }
    }

    public List<CategoryResponse> searchCategories(String name) {
        if (!StringUtils.hasText(name) || name.trim().length() < 2) {
            throw new DataValidationException("Search term must be at least 2 characters long");
        }

        try {
            return categoryRepository.searchActiveCategoriesByName(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error searching categories", e);
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        return objectMapper.convertValue(category, CategoryResponse.class);
    }
}
