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
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final CategoryRepository categoryRepository;
    private final ProductPatchMapper patchMapper;
    private final ProductCreateMapper createMapper;
    private final ProductResponseMapper responseMapper;

    public List<ProductResponse> getAllAvailableProducts() {
        return responseMapper.toResponseList(
            productRepository.findByIsAvailableTrue()
        );
    }

    public ProductResponse getProductById(Long id) {
        validateId(id, "product");

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));

        return responseMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(@Valid ProductRequest request) {
        validatePricingRules(request);

        Restaurant restaurant =
            restaurantService.getRestaurantEntity(request.getRestaurantId());

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                    )
                );
        }

        Product product = createMapper.toEntity(request);
        product.setRestaurant(restaurant);
        product.setCategory(category);

        return responseMapper.toResponse(
            productRepository.save(product)
        );
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validatePricingRules(request);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));

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
        return responseMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        validateId(id, "product");

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: " + id
            ));

        product.setIsAvailable(false);
        productRepository.save(product);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return responseMapper.toResponseList(
            productRepository.findByIsFeaturedTrueAndIsAvailableTrue()
        );
    }

    public List<ProductResponse> getProductsByPriceRange(
        Long restaurantId,
        BigDecimal minPrice,
        BigDecimal maxPrice
    ) {
        validatePriceRange(minPrice, maxPrice);

        return responseMapper.toResponseList(
            productRepository.findByRestaurantIdAndPriceBetweenAndIsAvailableTrue(
                restaurantId,
                minPrice,
                maxPrice
            )
        );
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId) {
        validateId(restaurantId, "restaurant");

        return responseMapper.toResponseList(
            productRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
        );
    }

    public List<ProductResponse> searchProducts(String name) {
        return responseMapper.toResponseList(
            productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(name)
        );
    }

    public List<ProductResponse> getProductsByRestaurant(Long restaurantId, Long categoryId) {
        validateId(restaurantId, "restaurant");
        validateId(categoryId, "category");

        return responseMapper.toResponseList(
            productRepository.findByRestaurantIdAndCategoryIdAndIsAvailableTrue(
                restaurantId,
                categoryId
            )
        );
    }

    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return responseMapper.toResponseList(
            productRepository.findByCategoryIdAndIsAvailableTrue(categoryId)
        );
    }

    public Long countProductsByRestaurant(Long restaurantId) {
        return productRepository.countByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

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

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new DataValidationException(
                "Invalid " +  fieldName + " ID"
            );
        }
    }

    private void validatePriceRange(BigDecimal minPrice,  BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new DataValidationException("Price range values must not be null");
        }

        if (minPrice.compareTo(BigDecimal.ZERO) < 0 ||
            maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DataValidationException("Price values must be zero or positive");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new DataValidationException("minPrice must be less than or equal to maxPrice");
        }
    }
}
