package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.CategoryRequest;
import com.quickbite.product_service.dto.CategoryResponse;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest validCategoryRequest;
    private Category activeCategory;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        validCategoryRequest = new CategoryRequest();
        validCategoryRequest.setName(TestConstants.VALID_CATEGORY_NAME);
        validCategoryRequest.setDescription(TestConstants.VALID_DESCRIPTION);
        validCategoryRequest.setImageUrl(TestConstants.VALID_IMAGE_URL);
        validCategoryRequest.setSortOrder(TestConstants.VALID_SORT_ORDER);
        validCategoryRequest.setIsActive(true);

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
    void createCategory_ShouldCreateCategorySuccessfully() {
        when(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(activeCategory);
        when(objectMapper.convertValue(any(Category.class), eq(CategoryResponse.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(validCategoryRequest);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_CATEGORY_NAME, result.getName());
        assertEquals(TestConstants.VALID_DESCRIPTION, result.getDescription());
        verify(categoryRepository).existsByName(TestConstants.VALID_CATEGORY_NAME);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowExceptionWhenCategoryNameExists() {
        when(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
            BusinessRuleViolationException.class,
            () -> categoryService.createCategory(validCategoryRequest));

        String expectedMessage = String.format(TestConstants.CATEGORY_ALREADY_EXISTS_MESSAGE,
            TestConstants.VALID_CATEGORY_NAME);
        assertEquals(expectedMessage, exception.getMessage());
        verify(categoryRepository).existsByName(TestConstants.VALID_CATEGORY_NAME);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowValidationExceptionWhenNameIsEmpty() {
        validCategoryRequest.setName("");

        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> categoryService.createCategory(validCategoryRequest));

        assertEquals(TestConstants.NAME_REQUIRED_MESSAGE, exception.getMessage());
        verify(categoryRepository, never()).existsByName(anyString());
    }

    @Test
    void createCategory_ShouldThrowsValidationExceptionWhenNameIsTooLong() {
        validCategoryRequest.setName(TestConstants.LONG_NAME);

        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> categoryService.createCategory(validCategoryRequest));

        assertEquals(TestConstants.NAME_TOO_LONG_MESSAGE, exception.getMessage());
    }

    @Test
    void getCategoryById_ShouldReturnCategoryWhenExists() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID)).thenReturn(Optional.of(activeCategory));
        when(objectMapper.convertValue(any(Category.class), eq(CategoryResponse.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryById(TestConstants.VALID_CATEGORY_ID);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_CATEGORY_ID, result.getId());
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
    }

    @Test
    void getCategoryById_ShouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(TestConstants.NON_EXISTENT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> categoryService.getCategoryById(TestConstants.NON_EXISTENT_ID));

        assertEquals(TestConstants.CATEGORY_NOT_FOUND_MESSAGE + TestConstants.NON_EXISTENT_ID,
            exception.getMessage());
        verify(categoryRepository).findById(TestConstants.NON_EXISTENT_ID);
    }

    @Test
    void getCategoryId_ShouldThrowValidationExceptionWhenIdIsInvalid() {
        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> categoryService.getCategoryById(TestConstants.INVALID_ID));

        assertEquals("Invalid category ID", exception.getMessage());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void getAllCategories_ShouldReturnActiveCategories() {
        List<Category> categories = List.of(activeCategory);
        when(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(categories);
        when(objectMapper.convertValue(any(Category.class), eq(CategoryResponse.class)))
            .thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(TestConstants.VALID_CATEGORY_NAME, result.getFirst().getName());
        verify(categoryRepository).findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Test
    void updateCategory_ShouldUpdateCategorySuccessfully() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName(TestConstants.UPDATED_CATEGORY_NAME);
        updateRequest.setDescription(TestConstants.UPDATED_DESCRIPTION);

        Category updatedCategory = Category.builder()
            .id(TestConstants.VALID_CATEGORY_ID)
            .name(TestConstants.UPDATED_CATEGORY_NAME)
            .description(TestConstants.UPDATED_DESCRIPTION)
            .build();

        CategoryResponse updatedResponse = new CategoryResponse();
        updatedResponse.setName(TestConstants.UPDATED_CATEGORY_NAME);

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID)).thenReturn(Optional.of(activeCategory));
        when(categoryRepository.existsByName(TestConstants.UPDATED_CATEGORY_NAME)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(objectMapper.convertValue(any(Category.class), eq(CategoryResponse.class)))
            .thenReturn(updatedResponse);

        CategoryResponse result = categoryService.updateCategory(TestConstants.VALID_CATEGORY_ID, updateRequest);

        assertNotNull(result);
        assertEquals(TestConstants.UPDATED_CATEGORY_NAME, result.getName());
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenNameAlreadyExists() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName(TestConstants.UPDATED_CATEGORY_NAME);

        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID)).thenReturn(Optional.of(activeCategory));
        when(categoryRepository.existsByName(TestConstants.UPDATED_CATEGORY_NAME)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
            () -> categoryService.updateCategory(TestConstants.VALID_CATEGORY_ID, updateRequest));

        String expectedMessage = String.format(TestConstants.CATEGORY_ALREADY_EXISTS_MESSAGE,
            TestConstants.UPDATED_CATEGORY_NAME);

        assertEquals(expectedMessage, exception.getMessage());
        verify(categoryRepository).existsByName(TestConstants.UPDATED_CATEGORY_NAME);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldSoftDeleteCategorySuccessfully() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID)).thenReturn(Optional.of(activeCategory));
        when(productRepository.countByRestaurantIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID)).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenReturn(activeCategory);

        categoryService.deleteCategory(TestConstants.VALID_CATEGORY_ID);

        assertFalse(activeCategory.getIsActive());
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(productRepository).countByRestaurantIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldThrowExceptionWhenCategoryHasProducts() {
        when(categoryRepository.findById(TestConstants.VALID_CATEGORY_ID)).thenReturn(Optional.of(activeCategory));
        when(productRepository.countByRestaurantIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID)).thenReturn(5L);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
            () -> categoryService.deleteCategory(TestConstants.VALID_CATEGORY_ID));

        assertTrue(exception.getMessage().contains(TestConstants.CATEGORY_HAS_PRODUCTS_MESSAGE));
        verify(categoryRepository).findById(TestConstants.VALID_CATEGORY_ID);
        verify(productRepository).countByRestaurantIdAndIsAvailableTrue(TestConstants.VALID_CATEGORY_ID);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void searchCategories_ShouldReturnCategoriesWhenValidSearchTerm() {
        List<Category> categories = List.of(activeCategory);

        when(categoryRepository.searchActiveCategoriesByName(TestConstants.VALID_SEARCH_TERM)).thenReturn(categories);
        when(objectMapper.convertValue(any(Category.class), eq(CategoryResponse.class))).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.searchCategories(TestConstants.VALID_SEARCH_TERM);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(categoryRepository).searchActiveCategoriesByName(TestConstants.VALID_SEARCH_TERM);
    }

    @Test
    void searchCategories_ShouldThrowExceptionWhenSearchTermTooSort() {
        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> categoryService.searchCategories(TestConstants.SEARCH_TERM_SHORT));

        assertEquals(TestConstants.SEARCH_TERM_TOO_SHORT_MESSAGE, exception.getMessage());
        verify(categoryRepository, never()).searchActiveCategoriesByName(anyString());
    }
}
