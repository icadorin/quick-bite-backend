package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import com.quickbite.product_service.repository.specification.CategorySpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnOnlyActiveCategories_whenFilterIsActiveTrue() {

        Category active = Category.builder()
            .name(TestConstants.ACTIVE_CATEGORY_NAME)
            .isActive(true)
            .build();

        Category inactive = Category.builder()
            .name(TestConstants.INACTIVE_CATEGORY_NAME)
            .isActive(false)
            .build();

        entityManager.persistAndFlush(active);
        entityManager.persistAndFlush(inactive);

        CategoryFilter filter = new CategoryFilter(null, true);
        var result = categoryRepository.findAll(CategorySpecification.withFilters(filter));

        assertEquals(1, result.size());
        Category first = result.getFirst();
        assertTrue(first.getIsActive());
        assertEquals(TestConstants.ACTIVE_CATEGORY_NAME, first.getName());
    }
}
