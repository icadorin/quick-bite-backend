package com.quickbite.product_service.service;

import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.mapper.ProductCreateMapper;
import com.quickbite.product_service.mapper.ProductPatchMapper;
import com.quickbite.product_service.mapper.ProductResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final CategoryRepository categoryRepository;
    private final ProductPatchMapper patchMapper;
    private final ProductCreateMapper productCreateMapper;
    private final ProductResponseMapper productResponseMapper;

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
            return productResponseMapper.toResponseList(
                productRepository.findByIsAvailableTrue()
            );
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving available products", e);
        }
    }

    public ProductResponse getProductById(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid product ID");
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));

        return productResponseMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateProductRequest(request);

        Restaurant restaurant = restaurantService.getRestaurantEntity(request.getRestaurantId());

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + request.getCategoryId()
                ));
        }

        Product product = productCreateMapper.toEntity(request);
        product.setRestaurant(restaurant);
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return productResponseMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateProductRequest(request);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Restaurant restaurant = restaurantService.getRestaurantEntity(request.getRestaurantId());
        product.setRestaurant(restaurant);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + request.getCategoryId())
                );
            product.setCategory(category);
        }

        patchMapper.updateProductFromRequest(request, product);

        Product updated = productRepository.save(product);
        return productResponseMapper.toResponse(updated);
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
            return productResponseMapper.toResponseList(
                productRepository.findByIsFeaturedTrueAndIsAvailableTrue()
            );
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
            return productResponseMapper.toResponseList(
                productRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
            );
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving products by price rage", e);
        }
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new DataValidationException("Valid restaurant ID is required");
        }

        return productResponseMapper.toResponseList(
            productRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
        );
    }

    public List<ProductResponse> searchProducts(String name) {
        return productResponseMapper.toResponseList(
            productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(name)
        );
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId, Long categoryId) {
        return productResponseMapper.toResponseList(
            productRepository.findByRestaurantIdAndCategoryIdAndIsAvailableTrue(restaurantId, categoryId)
        );
    }

    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productResponseMapper.toResponseList(
            productRepository.findByCategoryIdAndIsAvailableTrue(categoryId)
        );
    }

    public Long countProductsByRestaurant(Long restaurantId) {
        return productRepository.countByRestaurantIdAndIsAvailableTrue(restaurantId);
    }
}
