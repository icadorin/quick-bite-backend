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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private void validatePricingRules(ProductRequest request) {

        if (request.getComparePrice() != null &&
            request.getPrice().compareTo(request.getComparePrice()) >= 0) {
            throw new BusinessRuleViolationException(
                "Compare price should be greater than current price"
            );
        }

        if (request.getComparePrice() != null &&
            request.getPrice().compareTo(request.getCostPrice()) <= 0) {
            throw new BusinessRuleViolationException(
                "Price should be greater than cost price"
            );
        }
    }

    public List<ProductResponse> getAllAvailableProducts() {
        return productResponseMapper.toResponseList(
            productRepository.findByIsAvailableTrue()
        );
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
    public ProductResponse createProduct(@Valid ProductRequest request) {
        validatePricingRules(request);

        Restaurant restaurant =
            restaurantService.getRestaurantEntity(request.getRestaurantId());

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

        return productResponseMapper.toResponse(
            productRepository.save(product)
        );
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validatePricingRules(request);

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

    public List<ProductResponse> getProductsByPriceRange(
        Long restaurantId,
        BigDecimal minPrice,
        BigDecimal maxPrice
    ) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new DataValidationException("Valid restaurant ID is required");
        }

        if (minPrice == null || maxPrice == null) {
            throw new DataValidationException("Both MinPrice and MaxPrice are required");
        }

        if (minPrice.compareTo(BigDecimal.ZERO) < 0 ||
            maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DataValidationException("Price must be positive");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new DataValidationException("minPrice must be less than or equal to maxPrice");
        }

        return productResponseMapper.toResponseList(
            productRepository.findByRestaurantIdAndPriceBetweenAndIsAvailableTrue(
                restaurantId,
                minPrice,
                maxPrice
            )
        );
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
