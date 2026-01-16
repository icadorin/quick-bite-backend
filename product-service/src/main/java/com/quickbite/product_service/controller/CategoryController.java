package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.CATEGORIES)
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping(ApiPaths.BY_ID)
    public CategoryResponse getCategoryById(
        @PathVariable("id") @Positive Long id
    ) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping(ApiPaths.SEARCH)
    public List<CategoryResponse> searchCategories(
        @RequestParam @Size(min = 2) String name
    ) {
        return categoryService.searchCategories(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
        @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.createCategory(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    public CategoryResponse updateCategory(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
        @PathVariable("id") @Positive Long id
    ) {
        categoryService.deleteCategory(id);
    }
}
