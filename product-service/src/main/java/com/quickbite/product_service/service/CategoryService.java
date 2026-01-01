package com.quickbite.product_service.service;

import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.mapper.CategoryCreateMapper;
import com.quickbite.product_service.mapper.CategoryPatchMapper;
import com.quickbite.product_service.mapper.CategoryResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryCreateMapper createMapper;
    private final CategoryPatchMapper  patchMapper;
    private final CategoryResponseMapper responseMapper;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return responseMapper.toResponseList(categories);
    }

    public CategoryResponse getCategoryById(Long id) {
        validateId(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + id
            ));

        return responseMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException(
                "Category with name '" + trimmedName + "' already exists"
            );
        }

        Category category = createMapper.toEntity(request);
        category.setName(trimmedName);
        category.setDescription(
            request.getDescription() != null
                ? request.getDescription().trim()
                : null
        );
        category.setIsActive(
            request.getIsActive() != null
                ? request.getIsActive()
                : true);
        category.setSortOrder(
            request.getSortOrder() != null
                ? request.getSortOrder()
                : 0
        );

        Category savedCategory = categoryRepository.save(category);
        return responseMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        validateId(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();

        if (!category.getName().equals(trimmedName) &&
            categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException(
                "Category with name '" + trimmedName + "' already exists"
            );
        }

        patchMapper.updateCategoryFromRequest(request, category);
        Category updatedCategory = categoryRepository.save(category);
        return responseMapper.toResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        validateId(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: " + id
            ));

        Long productCount = productRepository.countByCategoryIdAndIsAvailableTrue(id);

        if (productCount > 0) {
            throw new BusinessRuleViolationException(
                "Cannot delete category with associated products. " +
                    "There are " + productCount + " products associated with this category."
            );
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    public List<CategoryResponse> searchCategories(String searchTerm) {
        validateSearchTerm(searchTerm);

        List<Category> categories =
            categoryRepository.searchActiveCategoriesByName(searchTerm);

        return responseMapper.toResponseList(categories);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid category ID");
        }
    }

    private void validateSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new DataValidationException("Search term must not be empty");
        }

        if (searchTerm.length() < 3) {
            throw new DataValidationException("Search term must be at least 3 characters");
        }
    }
}
