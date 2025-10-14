package com.quickbite.product_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public List<ProductResponse> getAllAvailableProducts() {
        return productRepository.findAll()
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

    public List<ProductResponse> getProductByRestaurant(Long restaurantId, Long categoryId) {
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
