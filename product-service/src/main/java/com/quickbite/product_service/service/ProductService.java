package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.mapper.ProductCreateMapper;
import com.quickbite.product_service.mapper.ProductPatchMapper;
import com.quickbite.product_service.mapper.ProductResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import com.quickbite.product_service.repository.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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

    public Page<ProductResponse> getProducts(
        ProductFilter filter,
        Pageable pageable
    ) {
        var specification =
            ProductSpecification.withFilters(
                filter == null
                    ? new ProductFilter(null, null, null, null, null)
                    : filter
            );

        return productRepository
            .findAll(specification, pageable)
            .map(responseMapper::toResponse);
    }

    public List<ProductResponse> getFeaturedProducts() {
        return responseMapper.toResponseList(
            productRepository.findByIsFeaturedTrueAndIsAvailableTrue()
        );
    }

    public ProductResponse getProductById(Long id) {
        validateId(id, "product");

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: %d".formatted(id)
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
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: %d".formatted(request.getCategoryId())
                ));
        }

        Product product = createMapper.toEntity(request);
        product.setRestaurant(restaurant);
        product.setCategory(category);

        return responseMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, @Valid ProductRequest request) {
        validatePricingRules(request);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: %d".formatted(id)
            ));

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantService.getRestaurantEntity(request.getRestaurantId());
            product.setRestaurant(restaurant);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: %d".formatted(id)
                ));
            product.setCategory(category);
        }

        patchMapper.updateProductFromRequest(request, product);

        return responseMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        validateId(id, "product");

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with id: %d".formatted(id)
            ));

        product.setIsAvailable(false);
        productRepository.save(product);
    }

    public Long countProductsByRestaurant(Long restaurantId) {
        validateId(restaurantId, "restaurant");
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
                "Invalid %s ID: %d".formatted(fieldName, id)
            );
        }
    }
}
