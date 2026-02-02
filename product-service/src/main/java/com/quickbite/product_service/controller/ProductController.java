package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
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
@RequestMapping(ApiPaths.PRODUCTS)
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> getAllAvailableProducts() {
        return productService.getAllAvailableProducts();
    }

    @GetMapping(ApiPaths.BY_ID)
    public ProductResponse getProductById(
        @PathVariable("id") @Positive Long id
    ) {
        return productService.getProductById(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<ProductResponse> getProductsByRestaurant(
        @PathVariable("restaurantId") @Positive Long restaurantId
    ) {
        return productService.getProductsByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/category/{categoryId}")
    public List<ProductResponse> getProductsByRestaurantAndCategory(
        @PathVariable("restaurantId") @Positive Long restaurantId,
        @PathVariable("categoryId") @Positive Long categoryId
    ) {
        return productService.getProductsByRestaurant(restaurantId, categoryId);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getProductsByCategory(
        @PathVariable("categoryId") @Positive Long categoryId
    ) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/featured")
    public List<ProductResponse> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
        @Valid @RequestBody ProductRequest request
    ) {
        return productService.createProduct(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    public ProductResponse updateProduct(
        @PathVariable("id") @Positive Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
        @PathVariable("id") @Positive Long id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiPaths.SEARCH)
    public List<ProductResponse> searchProducts(
        @RequestParam @Size(min = 3) String name
    ) {
        return productService.searchProducts(name);
    }

    @GetMapping("/restaurant/{restaurantId}/price-range")
    public List<ProductResponse> getProductsByPriceRange(
        @PathVariable("restaurantId") @Positive Long restaurantId,
        @Valid @ModelAttribute PriceRangeRequest priceRange
    ) {
        return productService.getProductsByPriceRange(
            restaurantId,
            priceRange.minPrice(),
            priceRange.maxPrice()
        );
    }

    @GetMapping("/restaurant/{restaurantId}/count")
    public Long countProductsByRestaurant(
        @PathVariable("restaurantId") @Positive Long restaurantId
    ) {
        return productService.countProductsByRestaurant(restaurantId);
    }
}
