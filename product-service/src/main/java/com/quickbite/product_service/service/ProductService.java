package com.quickbite.product_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public List<ProductResponse> getAllAvailableProducts() {
        return productRepository.findByIsAvailableTrue()
            .stream()
            .filter(Product::getIsAvailable)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not"));

        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        restaurantService.getRestaurantById(request.getRestaurantId());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + request.getCategoryId()
                ));
        }

        Product product = Product.builder()
            .restaurant(restaurantService.getRestaurantEntity(request.getRestaurantId()))
            .category(request.getCategoryId() != null ?
                    categoryRepository.findById(request.getCategoryId()).orElse(null) : null)
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .comparePrice(request.getComparePrice())
            .costPrice(request.getCostPrice())
            .imageUrl(request.getImageUrl())
            .ingredients(convertToJson(request.getIngredients()))
            .allergens(convertToJson(request.getAllergens()))
            .isAvailable(request.getIsAvailable())
            .isFeatured(request.getIsFeatured())
            .preparationTime(request.getPreparationTime())
            .calories(request.getCalories())
            .sortOrder(request.getSortOrder())
            .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        restaurantService.getRestaurantById(request.getRestaurantId());
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        product.setRestaurant(restaurantService.getRestaurantEntity(request.getRestaurantId()));
        product.setCategory(request.getCategoryId() != null ?
                categoryRepository.findById(request.getCategoryId()).orElse(null) : null);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setComparePrice(request.getComparePrice());
        product.setCostPrice(request.getCostPrice());
        product.setImageUrl(request.getImageUrl());
        product.setIngredients(convertToJson(request.getIngredients()));
        product.setAllergens(convertToJson(request.getAllergens()));
        product.setIsAvailable(request.getIsAvailable());
        product.setIsFeatured(request.getIsFeatured());
        product.setPreparationTime(request.getPreparationTime());
        product.setCalories(request.getCalories());
        product.setSortOrder(request.getSortOrder());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsAvailable(false);
        productRepository.save(product);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsAvailableTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByPriceRange(Long restaurantId, Double minPrice, Double maxPrice) {
        return productRepository.findByRestaurantIdAndPriceBetweenAndIsAvailableTrue(
            restaurantId,
            BigDecimal.valueOf(minPrice),
            BigDecimal.valueOf(maxPrice)
        )
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId) {
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
        ProductResponse response = objectMapper.convertValue(product, ProductResponse.class);

        response.setRestaurantId(product.getRestaurant().getId());
        response.setRestaurantName(product.getRestaurant().getName());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        try {
            if (product.getIngredients() != null) {
                response.setIngredients(
                    objectMapper.readValue(product.getIngredients(),
                    new TypeReference<List<String>>() {}
                ));
            }

            if (product.getAllergens() != null) {
                response.setAllergens(objectMapper.readValue(
                    product.getAllergens(),
                    new TypeReference<List<String>>() {}
                ));
            }
        } catch (Exception e) {
            //
        }

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
