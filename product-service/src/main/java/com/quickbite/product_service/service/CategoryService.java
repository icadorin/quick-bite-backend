package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.mapper.CategoryCreateMapper;
import com.quickbite.product_service.mapper.CategoryPatchMapper;
import com.quickbite.product_service.mapper.CategoryResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import com.quickbite.product_service.repository.specification.CategorySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryCreateMapper createMapper;
    private final CategoryPatchMapper patchMapper;
    private final CategoryResponseMapper responseMapper;

    public Page<CategoryResponse> getCategories(
        CategoryFilter filter,
        Pageable pageable
    ) {
        var safeFilter = filter == null
            ? new CategoryFilter(null, true)
            : filter;

        var specification =
            CategorySpecification.withFilters(safeFilter);

        return categoryRepository
            .findAll(specification, pageable)
            .map(responseMapper::toResponse);
    }

    public CategoryResponse getCategoryById(Long id) {
        validateId(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: %d".formatted(id)
            ));

        return responseMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();

        if (categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException(
                "Category with name '%s' already exists".formatted(trimmedName)
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

        Category saved = categoryRepository.save(category);
        return responseMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        validateId(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category not found with id: %d".formatted(id)
            ));

        String trimmedName = request.getName().trim();

        if (!category.getName().equals(trimmedName) &&
                categoryRepository.existsByName(trimmedName)) {
            throw new BusinessRuleViolationException(
                "Category with name '%s' already exists".formatted(trimmedName)
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
                "Category not found with id: %d".formatted(id)
            ));

        Long productCount = productRepository.countByCategoryIdAndIsAvailableTrue(id);

        if (productCount > 0) {
            throw new BusinessRuleViolationException(
                ("Cannot delete category with associated products. " +
                    "There are %d products linked.").formatted(productCount)
            );
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid category ID: %d".formatted(id));
        }
    }
}
