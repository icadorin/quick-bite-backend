package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    private void validateProductRequest(ProductRequest request) {
        if (request.getRestaurantId() == null || request.getRestaurantId() <= 0) {
            throw new DataValidationException("Valid restaurant ID is required");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new DataValidationException("Product name is required");
        }

        if (request.getName().length() > 255) {
            throw new DataValidationException("Product name must not exceed 255 characters");
        }

        if (request.getPrice() == null) {
            throw new DataValidationException("Product price is required");
        }

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DataValidationException("Product price must be greater than zero");
        }

        if (request.getComparePrice() != null && request.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new DataValidationException("Cost price must be positive");
        }

        if (request.getPreparationTime() != null && request.getPreparationTime() < 0) {
            throw new DataValidationException("Preparation time must be positive");
        }

        if (request.getCalories() != null && request.getCalories() < 0) {
            throw new DataValidationException("Calories must be positive");
        }

        if (request.getSortOrder() != null && request.getSortOrder() < 0) {
            throw new DataValidationException("Sort order must be positive");
        }

        if (request.getComparePrice() != null && request.getPrice().compareTo(request.getComparePrice()) >= 0) {
            throw new BusinessRuleViolationException("Compare price should be greater than current price");
        }

        if (request.getComparePrice() != null && request.getPrice().compareTo(request.getCostPrice()) <= 0) {
            throw new BusinessRuleViolationException("Price should be greater than cost price");
        }
    }

    public List<ProductResponse> getAllAvailableProducts() {
        try {
            return productRepository.findByIsAvailableTrue()
                .stream()
                .filter(Product::getIsAvailable)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving available products", e);
        }
    }

    public ProductResponse getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid product ID");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not"));

        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateProductRequest(request);

        restaurantService.getRestaurantById(request.getRestaurantId());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + request.getCategoryId()
                ));
        }

        try {
            Product product = Product.builder()
                .restaurant(restaurantService.getRestaurantEntity(request.getRestaurantId()))
                .category(request.getCategoryId() != null ?
                    categoryRepository.findById(request.getCategoryId()).orElse(null) : null)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .price(request.getPrice())
                .comparePrice(request.getComparePrice())
                .costPrice(request.getCostPrice())
                .imageUrl(request.getImageUrl())
                .ingredients(request.getIngredients())
                .allergens(request.getAllergens())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .preparationTime(request.getPreparationTime())
                .calories(request.getCalories())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

            Product savedProduct = productRepository.save(product);
            return mapToResponse(savedProduct);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error creating product", e);
        }
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid product ID");
        }

        validateProductRequest(request);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        restaurantService.getRestaurantById(request.getRestaurantId());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        try {
            product.setRestaurant(restaurantService.getRestaurantEntity(request.getRestaurantId()));
            product.setCategory(request.getCategoryId() != null ?
                categoryRepository.findById(request.getCategoryId()).orElse(null) : null);
            product.setName(request.getName());
            product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            product.setPrice(request.getPrice());
            product.setComparePrice(request.getComparePrice());
            product.setCostPrice(request.getCostPrice());
            product.setImageUrl(request.getImageUrl());
            product.setIngredients(request.getIngredients());
            product.setAllergens(request.getAllergens());
            product.setIsAvailable(request.getIsAvailable());
            product.setIsFeatured(request.getIsFeatured());
            product.setPreparationTime(request.getPreparationTime());
            product.setCalories(request.getCalories());
            product.setSortOrder(request.getSortOrder());

            if (request.getIsAvailable() != null) {
                product.setIsAvailable(request.getIsAvailable());
            }

            if (request.getIsAvailable() != null) {
                product.setIsFeatured(request.getIsFeatured());
            }

            product.setPreparationTime(request.getPreparationTime());
            product.setCalories(request.getCalories());
            product.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : product.getSortOrder());

            Product updatedProduct = productRepository.save(product);
            return mapToResponse(updatedProduct);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error updating product", e);
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid product ID");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        try {
            product.setIsAvailable(false);
            productRepository.save(product);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error deleting product", e);
        }
    }

    public List<ProductResponse> getFeaturedProducts() {
        try {
            return productRepository.findByIsFeaturedTrueAndIsAvailableTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving featured products", e);
        }
    }

    public List<ProductResponse> getProductsByPriceRange(Long restaurantId, Double minPrice, Double maxPrice) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new DataValidationException("Valid restaurant ID is required");
        }

        if (minPrice == null || maxPrice == null) {
            throw new DataValidationException("Both MinPrice and MaxPrice are required");
        }

        if (minPrice < 0 || maxPrice < 0) {
            throw new DataValidationException("Price must be positive");
        }

        if (minPrice > maxPrice) {
            throw new DataValidationException("minPrice must be less than or equal to maxPrice");
        }

        try {
            return productRepository.findByRestaurantIdAndPriceBetweenAndIsAvailableTrue(
                    restaurantId,
                    BigDecimal.valueOf(minPrice),
                    BigDecimal.valueOf(maxPrice)
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving products by price rage", e);
        }
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new DataValidationException("Valid restaurant ID is required");
        }

        return productRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(name)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId, Long categoryId) {
        return productRepository.findByRestaurantIdAndCategoryIdAndIsAvailableTrue(restaurantId, categoryId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsAvailableTrue(categoryId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public Long countProductsByRestaurant(Long restaurantId) {
        return productRepository.countByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setRestaurantId(product.getRestaurant().getId());
        response.setRestaurantName(product.getRestaurant().getName());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setComparePrice(product.getComparePrice());
        response.setCostPrice(product.getCostPrice());
        response.setImageUrl(product.getImageUrl());
        response.setIngredients(product.getIngredients());
        response.setAllergens(product.getAllergens());
        response.setIsAvailable(product.getIsAvailable());
        response.setIsFeatured(product.getIsFeatured());
        response.setPreparationTime(product.getPreparationTime());
        response.setCalories(product.getCalories());
        response.setSortOrder(product.getSortOrder());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting object to JSON", e);
        }
    }
}
