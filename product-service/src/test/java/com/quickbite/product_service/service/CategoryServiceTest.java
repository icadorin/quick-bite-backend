package com.quickbite.product_service.service;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
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
        validRequest = new CategoryRequest();
        validRequest.setName(TestConstants.VALID_CATEGORY_NAME);
        validRequest.setDescription(TestConstants.VALID_DESCRIPTION);
        validRequest.setImageUrl(TestConstants.VALID_IMAGE_URL);
        validRequest.setSortOrder(TestConstants.VALID_SORT_ORDER);
        validRequest.setIsActive(true);

        activeCategory = Category.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.VALID_CATEGORY_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .imageUrl(TestConstants.VALID_IMAGE_URL)
            .sortOrder(TestConstants.VALID_SORT_ORDER)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(TestConstants.VALID_CATEGORY_ID);
        categoryResponse.setName(TestConstants.VALID_CATEGORY_NAME);
        categoryResponse.setDescription(TestConstants.VALID_DESCRIPTION);
        categoryResponse.setImageUrl(TestConstants.VALID_IMAGE_URL);
        categoryResponse.setSortOrder(TestConstants.VALID_SORT_ORDER);
        categoryResponse.setIsActive(true);
    }

    @Test
    void createCategory_shouldCreateSuccessfully() {
        when(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME)).thenReturn(false);
        when(createMapper.toEntity(validRequest)).thenReturn(activeCategory);
        when(categoryRepository.save(activeCategory)).thenReturn(activeCategory);
        when(responseMapper.toResponse(activeCategory)).thenReturn(categoryResponse);      

        CategoryResponse result = categoryService.createCategory(validRequest);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_CATEGORY_NAME, result.getName());
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
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID))
            .thenReturn(Optional.of(activeCategory));
        when(responseMapper.toResponse(activeCategory)).thenReturn(categoryResponse);

        CategoryResponse result = 
            categoryService.getCategoryById(TestConstants.VALID_CATEGORY_ID);

        assertNotNull(result);
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
    }


    @Test
    void getCategoryById_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.getCategoryById(TestConstants.NON_EXISTENT_ID));
    }

    @Test
    void getAllCategories_shouldReturnActiveCategories() {
        when(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc())
            .thenReturn(List.of(activeCategory));
        when(responseMapper.toResponseList(any()))
            .thenReturn(List.of(categoryResponse));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        verify(categoryRepository).findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName(TestConstants.UPDATED_CATEGORY_NAME);

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

        assertNotNull(result);
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
        assertEquals(1, result.size());
    }

    @Test
    void searchCategories_shouldThrow_whenSearchTermIsTooShort() {
        assertThrows(
            DataValidationException.class,
            () -> categoryService.searchCategories(TestConstants.SEARCH_TERM_SHORT)
        );
    }
}
