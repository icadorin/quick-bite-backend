package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.CATEGORIES)
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public Page<CategoryResponse> getCategories(
        CategoryFilter filter,
        @PageableDefault(size = 50, sort = "name") Pageable pageable
    ) {
        return service.getCategories(filter, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.createCategory(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(
        @PathVariable("id") @Positive Long id,
        @Valid @RequestBody CategoryRequest request
    ) {
        return service.updateCategory(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
