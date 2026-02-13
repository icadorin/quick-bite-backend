package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.ProductRequest;
import com.quickbite.product_service.dto.ProductResponse;
import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.mapper.ProductCreateMapper;
import com.quickbite.product_service.mapper.ProductResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductCreateMapper createMapper;

    @Mock
    private ProductResponseMapper responseMapper;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validProductRequest;
    private Product activeProduct;
    private Restaurant sampleRestaurant;
    private Category sampleCategory;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        sampleRestaurant = Restaurant.builder()
            .id(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .isActive(true)
            .build();

        sampleCategory = Category.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_CATEGORY_NAME)
            .isActive(true)
            .build();

        validProductRequest = ProductRequest.builder()
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .categoryId(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .costPrice(BigDecimal.valueOf(TestConstants.VALID_COST_PRICE))
            .isAvailable(true)
            .build();

        activeProduct = Product.builder()
            .id(TestConstants.VALID_PRODUCT_ID)
            .restaurant(sampleRestaurant)
            .category(sampleCategory)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .isAvailable(true)
            .build();

        productResponse = ProductResponse.builder()
            .id(TestConstants.VALID_PRODUCT_ID)
            .name(TestConstants.VALID_PRODUCT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .restaurantId(TestConstants.VALID_RESTAURANT_ID)
            .restaurantName(TestConstants.VALID_RESTAURANT_NAME)
            .categoryId(TestConstants.VALID_CATEGORY_ID)
            .categoryName(TestConstants.VALID_CATEGORY_NAME)
            .build();
    }

    @Test
    void createProduct_shouldCreateSuccessfully() {
        when(restaurantService.getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(sampleRestaurant);
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(sampleCategory));
        when(createMapper.toEntity(validProductRequest))
            .thenReturn(activeProduct);
        when(productRepository.save(any(Product.class)))
            .thenReturn(activeProduct);
        when(responseMapper.toResponse(activeProduct))
            .thenReturn(productResponse);

        ProductResponse result = productService.createProduct(validProductRequest);

        assertEquals(productResponse, result);

        verify(restaurantService).getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID);
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(productRepository).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
        verify(createMapper).toEntity(validProductRequest);
        verify(responseMapper).toResponse(activeProduct);
    }

    @Test
    void createProduct_shouldThrow_whenRestaurantNotFound() {
        when(restaurantService.getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID))
            .thenThrow(new ResourceNotFoundException(
                TestConstants.RESTAURANT_NOT_FOUND_MESSAGE + TestConstants.VALID_RESTAURANT_ID
            ));

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.createProduct(validProductRequest)
        );

        assertEquals(
            TestConstants.RESTAURANT_NOT_FOUND_MESSAGE + TestConstants.VALID_RESTAURANT_ID,
            exception.getMessage()
        );

        verify(restaurantService).getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID);
        verifyNoMoreInteractions(productRepository, createMapper, responseMapper);
    }

    @Test
    void createProduct_shouldThrow_whenCategoryNotFound() {

        when(restaurantService.getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(sampleRestaurant);

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.createProduct(validProductRequest)
        );

        assertTrue(ex.getMessage().contains("Category"));

        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verifyNoInteractions(productRepository);
        verify(restaurantService).getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID);
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        when(productRepository.findById(TestConstants.VALID_PRODUCT_ID))
            .thenReturn(Optional.of(activeProduct));
        when(responseMapper.toResponse(activeProduct))
            .thenReturn(productResponse);

        ProductResponse result =
            productService.getProductById(TestConstants.VALID_PRODUCT_ID);

        assertEquals(productResponse, result);

        verify(productRepository).findById(TestConstants.VALID_PRODUCT_ID);
        verify(responseMapper).toResponse(activeProduct);
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.getProductById(TestConstants.NON_EXISTENT_ID)
        );

        assertEquals(
            TestConstants.PRODUCT_NOT_FOUND_MESSAGE + TestConstants.NON_EXISTENT_ID,
            exception.getMessage()
        );

        verify(productRepository).findById(TestConstants.NON_EXISTENT_ID);
        verify(responseMapper, never()).toResponse(any());
    }

    @Test
    void getFeaturedProducts_shouldReturnFeaturedProducts() {
        List<Product> products = List.of(activeProduct);
        List<ProductResponse> responses  = List.of(productResponse);

        when(productRepository.findByIsFeaturedTrueAndIsAvailableTrue())
            .thenReturn(products);
        when(responseMapper.toResponseList(products))
            .thenReturn(responses);

        List<ProductResponse> result = productService.getFeaturedProducts();

        assertEquals(responses, result);

        verify(productRepository).findByIsFeaturedTrueAndIsAvailableTrue();
        verify(responseMapper).toResponseList(products);
    }

    @Test
    void deleteProduct_shouldSoftDeleteSuccessfully() {
        when(productRepository.findById(TestConstants.VALID_PRODUCT_ID))
            .thenReturn(Optional.of(activeProduct));
        when(productRepository.save(any(Product.class)))
            .thenReturn(activeProduct);

        productService.deleteProduct(TestConstants.VALID_PRODUCT_ID);

        verify(productRepository).findById(TestConstants.VALID_PRODUCT_ID);
        verify(productRepository).save(argThat(p ->
            Boolean.FALSE.equals(p.getIsAvailable())
        ));
    }

    @Test
    void deleteProduct_shouldThrow_whenNotFound() {
        when(productRepository.findById(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> productService.deleteProduct(TestConstants.NON_EXISTENT_ID)
        );

        assertEquals(
            "Product not found with id: %d".formatted(TestConstants.NON_EXISTENT_ID),
            ex.getMessage()
        );

        verify(productRepository).findById(TestConstants.NON_EXISTENT_ID);
    }

    @Test
    void getProducts_shouldReturnFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Product> mockPage = new PageImpl<>(List.of(activeProduct));

        when(productRepository.findAll(
            ArgumentMatchers.<Specification<Product>>any(),
            eq(pageable)
        )).thenReturn(mockPage);

        when(responseMapper.toResponse(activeProduct))
            .thenReturn(productResponse);

        ProductFilter filter = new ProductFilter(
            null,
            null,
            TestConstants.VALID_PRODUCT_NAME,
            null,
            null
        );

        Page<ProductResponse> result = productService.getProducts(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(productResponse, result.getContent().getFirst());
        verify(productRepository).findAll(
            ArgumentMatchers.<Specification<Product>>any(),
            eq(pageable)
        );
    }

    @Test
    void getProducts_shouldUseDefaultFilter_whenFilterIsNull() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Product> mockPage = new PageImpl<>(List.of(activeProduct));

        when(productRepository.findAll(
            ArgumentMatchers.<Specification<Product>>any(),
            eq(pageable)
        )).thenReturn(mockPage);

        when(responseMapper.toResponse(activeProduct))
            .thenReturn(productResponse);

        Page<ProductResponse> result = productService.getProducts(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(
            ArgumentMatchers.<Specification<Product>>any(),
            eq(pageable)
        );
    }

    @Test
    void createProduct_shouldThrow_whereComparePriceInvalid() {
        ProductRequest invalidRequest = validProductRequest.toBuilder()
            .price(BigDecimal.valueOf(100))
            .comparePrice(BigDecimal.valueOf(50))
            .build();

        BusinessRuleViolationException ex = assertThrows(
            BusinessRuleViolationException.class,
            () -> productService.createProduct(invalidRequest)
        );

        assertEquals(
            "Compare price should be greater than current price",
            ex.getMessage()
        );
        verifyNoInteractions(productRepository);
    }
}
