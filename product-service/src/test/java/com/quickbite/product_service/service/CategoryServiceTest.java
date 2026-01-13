package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.mapper.CategoryCreateMapper;
import com.quickbite.product_service.mapper.CategoryPatchMapper;
import com.quickbite.product_service.mapper.CategoryResponseMapper;
import com.quickbite.product_service.repository.CategoryRepository;
import com.quickbite.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void getAllCategories_shouldReturnActiveCategories() {
        when(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc())
            .thenReturn(List.of(activeCategory));
        when(responseMapper.toResponseList(any()))
            .thenReturn(List.of(categoryResponse));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertFalse(result.isEmpty());
        assertEquals(categoryResponse, result.getFirst());

        verify(categoryRepository).findByIsActiveTrueOrderBySortOrderAsc();
        verify(responseMapper).toResponseList(any());
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName(TestConstants.UPDATED_CATEGORY_NAME);

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(categoryRepository.existsByName(TestConstants.UPDATED_CATEGORY_NAME))
            .thenReturn(false);
        when(categoryRepository.save(activeCategory))
            .thenReturn(activeCategory);
        when(responseMapper.toResponse(activeCategory))
            .thenReturn(categoryResponse);

        doNothing().when(patchMapper)
            .updateCategoryFromRequest(updateRequest, activeCategory);

        CategoryResponse result =
            categoryService.updateCategory(TestConstants.VALID_CATEGORY_ID, updateRequest);

        assertEquals(categoryResponse, result);

        verify(patchMapper).updateCategoryFromRequest(updateRequest, activeCategory);
        verify(categoryRepository).save(activeCategory);
        verify(patchMapper).updateCategoryFromRequest(updateRequest, activeCategory);
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

    @Test
    void searchCategories_shouldReturnResults_whenSearchTermIsValid() {
        when(categoryRepository.searchActiveCategoriesByName(TestConstants.VALID_SEARCH_TERM))
            .thenReturn(List.of(activeCategory));
        when(responseMapper.toResponseList(any()))
            .thenReturn(List.of(categoryResponse));

        List<CategoryResponse> result =
            categoryService.searchCategories(TestConstants.VALID_SEARCH_TERM);

        assertFalse(result.isEmpty());
        assertEquals(categoryResponse, result.getFirst());
    }

    @Test
    void searchCategories_shouldThrow_whenSearchTermIsTooShort() {
        assertThrows(
            DataValidationException.class,
            () -> categoryService.searchCategories(TestConstants.SEARCH_TERM_SHORT)
        );
    }
}
