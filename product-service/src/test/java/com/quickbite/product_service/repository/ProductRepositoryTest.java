package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.repository.specification.ProductSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnAvailableProducts_whenUsingSpecification() {

        Restaurant restaurant = entityManager.persist(
            Restaurant.builder()
                .name(TestConstants.VALID_RESTAURANT_NAME)
                .ownerId(TestConstants.VALID_OWNER_ID)
                .isActive(true)
                .build()
        );

        Product product = entityManager.persist(
            Product.builder()
                .name(TestConstants.VALID_PRODUCT_NAME)
                .price(BigDecimal.TEN)
                .restaurant(restaurant)
                .build()
        );

        entityManager.flush();

        var filter = new ProductFilter(
            null,
            null,
            TestConstants.VALID_PRODUCT_NAME,
            null,
            null
        );

        var result = productRepository.findAll(
            ProductSpecification.withFilters(filter)
        );

        assertEquals(1, result.size());
    }
}
