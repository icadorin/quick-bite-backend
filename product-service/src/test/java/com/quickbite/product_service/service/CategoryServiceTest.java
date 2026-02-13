package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.mapper.CategoryCreateMapper;
import com.quickbite.product_service.mapper.CategoryPatchMapper;
import com.quickbite.product_service.mapper.CategoryResponseMapper;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryCreateMapper createMapper;

    @Mock
    private CategoryPatchMapper patchMapper;

    @Mock
    private CategoryResponseMapper responseMapper;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest validRequest;
    private Category activeCategory;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        validRequest = CategoryRequest.builder()
            .name(TestConstants.VALID_CATEGORY_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .imageUrl(TestConstants.VALID_IMAGE_URL)
            .sortOrder(TestConstants.VALID_SORT_ORDER)
            .isActive(true)
            .build();

        activeCategory = Category.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_CATEGORY_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .imageUrl(TestConstants.VALID_IMAGE_URL)
            .sortOrder(TestConstants.VALID_SORT_ORDER)
            .isActive(true)
            .build();

        categoryResponse = CategoryResponse.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_CATEGORY_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .imageUrl(TestConstants.VALID_IMAGE_URL)
            .sortOrder(TestConstants.VALID_SORT_ORDER)
            .isActive(true)
            .build();
    }

    @Test
    void createCategory_shouldCreateSuccessfully() {
        when(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME))
            .thenReturn(false);
        when(createMapper.toEntity(validRequest))
            .thenReturn(activeCategory);
        when(categoryRepository.save(activeCategory))
            .thenReturn(activeCategory);
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(validRequest);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(TestConstants.VALID_CATEGORY_NAME, result.getName())
        );

        verify(createMapper).toEntity(validRequest);
        verify(categoryRepository).save(activeCategory);
    }

    @Test
    void createCategory_shouldThrow_whenNameAlreadyExists() {
        when(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME))
            .thenReturn(true);

        BusinessRuleViolationException ex = assertThrows(
            BusinessRuleViolationException.class,
            () -> categoryService.createCategory(validRequest));

        String expectedMessage = String.format(
            TestConstants.CATEGORY_ALREADY_EXISTS_MESSAGE,
            TestConstants.VALID_CATEGORY_NAME
        );

        assertEquals(expectedMessage, ex.getMessage());

        verify(categoryRepository).existsByName(TestConstants.VALID_CATEGORY_NAME);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        CategoryResponse result = 
            categoryService.getCategoryById(TestConstants.VALID_CATEGORY_ID);

        assertEquals(categoryResponse, result);

        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(responseMapper).toResponse(activeCategory);
    }

    @Test
    void getCategoryById_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.getCategoryById(TestConstants.NON_EXISTENT_ID));

        verify(categoryRepository).findById(TestConstants.NON_EXISTENT_ID);
    }

    @Test
    void getCategories_shouldReturnFilteredCategories() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Category> mockPage = new PageImpl<>(List.of(activeCategory));

        when(categoryRepository.findAll(
            ArgumentMatchers.<Specification<Category>>any(),
            eq(pageable)
        )).thenReturn(mockPage);
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        CategoryFilter filter = new CategoryFilter(null, true);

        Page<CategoryResponse> result = categoryService.getCategories(filter, pageable);

        assertEquals(1, result.getTotalElements());

        verify(categoryRepository).findAll(
            ArgumentMatchers.<Specification<Category>>any(),
            eq(pageable)
        );
        verify(responseMapper).toResponse(activeCategory);
    }

    @Test
    void getCategories_shouldUseDefaultFilter_whenFilterIsNull() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Category> mockPage = new PageImpl<>(List.of(activeCategory));

        when(categoryRepository.findAll(
            ArgumentMatchers.<Specification<Category>>any(),
            eq(pageable))
        ).thenReturn(mockPage);
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        Page<CategoryResponse> result = categoryService.getCategories(null, pageable);

        assertEquals(1, result.getTotalElements());

        verify(categoryRepository).findAll(
            ArgumentMatchers.<Specification<Category>>any(),
            eq(pageable)
        );

        verify(responseMapper).toResponse(activeCategory);
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {
        CategoryRequest updateRequest = CategoryRequest.builder()
            .name(TestConstants.UPDATED_CATEGORY_NAME)
            .build();

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(categoryRepository.existsByName(TestConstants.UPDATED_CATEGORY_NAME))
            .thenReturn(false);
        when(categoryRepository.save(activeCategory)).thenReturn(activeCategory);
        when(responseMapper.toResponse(activeCategory)).thenReturn(categoryResponse);

        doNothing().when(patchMapper)
            .updateCategoryFromRequest(updateRequest, activeCategory);

        CategoryResponse result =
            categoryService.updateCategory(TestConstants.VALID_CATEGORY_ID, updateRequest);

        assertEquals(categoryResponse, result);

        verify(patchMapper).updateCategoryFromRequest(updateRequest, activeCategory);
        verify(categoryRepository).save(activeCategory);
    }

    @Test
    void deleteCategory_shouldSoftDeleteSuccessfully() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(productRepository.countByCategoryIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(TestConstants.NO_ASSOCIATED_PRODUCTS);

        categoryService.deleteCategory(TestConstants.VALID_CATEGORY_ID);

        assertFalse(activeCategory.getIsActive());

        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(categoryRepository).save(activeCategory);
    }

    @Test
    void createCategory_shouldTrimNameBeforeSaving() {
        CategoryRequest request = CategoryRequest.builder()
            .name(TestConstants.CATEGORY_NAME_WITH_SPACES)
            .build();

        Category category = Category.builder().build();

        when(categoryRepository.existsByName(TestConstants.CATEGORY_NAME_TRIMMED))
            .thenReturn(false);
        when(createMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(responseMapper.toResponse(category)).thenReturn(categoryResponse);

        categoryService.createCategory(request);

        verify(createMapper).toEntity(request);
        verify(categoryRepository).existsByName(TestConstants.CATEGORY_NAME_TRIMMED);
        verify(categoryRepository).save(argThat((cat ->
            TestConstants.CATEGORY_NAME_TRIMMED.equals(cat.getName()))
        ));
    }

    @Test
    void createCategory_shouldApplyDefaultValues() {
        CategoryRequest request = CategoryRequest.builder()
            .name(TestConstants.CATEGORY_NAME_WITH_SPACES)
            .isActive(null)
            .sortOrder(null)
            .build();

        Category category = Category.builder().build();

        when(categoryRepository.existsByName(TestConstants.CATEGORY_NAME_TRIMMED))
            .thenReturn(false);
        when(createMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(responseMapper.toResponse(category)).thenReturn(categoryResponse);

        categoryService.createCategory(request);

        assertTrue(category.getIsActive());
        verify(categoryRepository).save(argThat(cat ->
            cat.getIsActive() &&
            cat.getSortOrder() == 0
        ));
    }

    @Test
    void getCategoryById_shouldThrow_whenIdIsNull() {
        assertThrows(
            DataValidationException.class,
            () -> categoryService.getCategoryById(null)
        );
    }

    @Test
    void getCategoryById_shouldThrow_whenIdIsInvalid() {
        assertThrows(
            DataValidationException.class,
            () -> categoryService.getCategoryById(0L)
        );
    }

    @Test
    void updateCategory_shouldThrow_whenNameAlreadyExists() {
        CategoryRequest request = CategoryRequest.builder()
            .name(TestConstants.UPDATED_CATEGORY_NAME)
            .build();

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(categoryRepository.existsByName(TestConstants.UPDATED_CATEGORY_NAME))
            .thenReturn(true);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> categoryService.updateCategory(
                TestConstants.VALID_CATEGORY_ID,
                request
            )
        );

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_shouldNotCheckDuplicate_whenNameIsSame() {
        CategoryRequest request = CategoryRequest.builder()
            .name(activeCategory.getName())
            .build();

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(categoryRepository.save(activeCategory))
            .thenReturn(activeCategory);
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        CategoryResponse result =
            categoryService.updateCategory(TestConstants.VALID_CATEGORY_ID, request);

        assertEquals(categoryResponse, result);

        verify(categoryRepository, never()).existsByName(any());
    }

    @Test
    void deleteCategory_shouldThrow_whenHasAssociatedProducts() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(productRepository.countByCategoryIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(TestConstants.HAS_ASSOCIATED_PRODUCTS);

        assertThrows(
            BusinessRuleViolationException.class,
            () -> categoryService.deleteCategory(TestConstants.VALID_CATEGORY_ID)
        );

        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(productRepository)
            .countByCategoryIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID);
        verify(categoryRepository, never()).save(any());
    }
}
