package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByName_shouldReturnCategory_whenExists() {
        Category category = Category.builder()
            .name(TestConstants.VALID_CATEGORY_NAME)
            .isActive(true)
            .build();

        entityManager.persist(category);
        entityManager.flush();

        Optional<Category> result =
            categoryRepository.findByName(TestConstants.VALID_CATEGORY_NAME);

        assertTrue(result.isPresent());
        assertEquals(TestConstants.VALID_CATEGORY_NAME, result.get().getName());
    }

    @Test
    void findByIsActiveTrueOrderBySortOrderAsc_shouldReturnOnlyActiveCategories() {
        Category active = Category.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .sortOrder(TestConstants.VALID_SORT_ORDER)
            .isActive(true)
            .build();

        Category inactive = Category.builder()
            .name(TestConstants.UPDATED_CATEGORY_NAME)
            .sortOrder(TestConstants.ZERO_SORT_ORDER)
            .isActive(false)
            .build();

        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();

        List<Category> result =
            categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getIsActive());
    }

    @Test
    void searchActiveCategoriesByName_shouldReturnMatchingActiveCategories() {
        Category category = Category.builder()
            .name(TestConstants.VALID_CATEGORY_NAME)
            .isActive(true)
            .build();

        entityManager.persist(category);
        entityManager.flush();

        List<Category> result =
            categoryRepository.searchActiveCategoriesByName(
                TestConstants.VALID_SEARCH_TERM
            );

        assertEquals(1, result.size());
        assertEquals(TestConstants.VALID_CATEGORY_NAME, result.getFirst().getName());
    }

    @Test
    void existsByName_shouldReturnTrue_whenCategoryExists() {
        Category category = Category.builder()
            .name(TestConstants.VALID_CATEGORY_NAME)
            .isActive(true)
            .build();

        entityManager.persist(category);
        entityManager.flush();

        assertTrue(categoryRepository.existsByName(TestConstants.VALID_CATEGORY_NAME));
    }
}
