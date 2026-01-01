package com.quickbite.product_service.controller;

import com.quickbite.product_service.dto.PriceRangeRequest;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllAvailableProducts() {
        return ResponseEntity.ok(productService.getAllAvailableProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ProductResponse>> getProductsByRestaurant(
        @PathVariable @Positive Long restaurantId
    ) {
        return ResponseEntity.ok(productService.getProductsByRestaurant(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByRestaurantAndCategory(
        @PathVariable @Positive Long restaurantId,
        @PathVariable @Positive Long categoryId
    ) {
        return ResponseEntity.ok(
            productService.getProductsByRestaurant(restaurantId, categoryId)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
        @PathVariable @Positive Long categoryId
    ) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable @Positive Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
        @PathVariable @Positive Long id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
        @RequestParam @Size(min = 2) String name
    ) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @GetMapping("/restaurant/{restaurantId}/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
        @PathVariable @Positive Long restaurantId,
        @Valid @ModelAttribute PriceRangeRequest priceRange
        ) {
        return ResponseEntity.ok(
            productService.getProductsByPriceRange(
                restaurantId,
                priceRange.minPrice(),
                priceRange.maxPrice()
        ));
    }

    @GetMapping("/restaurant/{restaurantId}/count")
    public ResponseEntity<Long> countProductsByRestaurant(
        @PathVariable @Positive Long restaurantId
    ) {
        return ResponseEntity.ok(
            productService.countProductsByRestaurant(restaurantId)
        );
    }
}
