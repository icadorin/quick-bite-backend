package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PRODUCTS)
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService service;

    @GetMapping
    public Page<ProductResponse> getProducts(
        ProductFilter filter,
        @PageableDefault(
            size = 20,
            sort = "name",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        return service.getProducts(filter, pageable);
    }

    @GetMapping(ApiPaths.BY_ID)
    public ProductResponse getById(@PathVariable("id") @Positive Long id) {
        return service.getProductById(id);
    }

    @GetMapping("/featured")
    public List<ProductResponse> getFeatured() {
        return service.getFeaturedProducts();
    }

    @GetMapping("/by-restaurant/{restaurantId}/count")
    public Long countByRestaurant(@PathVariable @Positive Long restaurantId) {
        return service.countProductsByRestaurant(restaurantId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return service.createProduct(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ProductResponse update(
        @PathVariable("id") @Positive Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return service.updateProduct(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
