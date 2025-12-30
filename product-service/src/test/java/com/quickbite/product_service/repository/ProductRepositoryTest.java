package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.entity.Product;
import com.quickbite.product_service.entity.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

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
    void shouldReturnOnlyAvailableProducts_whenRestaurantIsActiveAndNameMatches() {
        Restaurant activeRestaurant = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .isActive(true)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .build();

        Restaurant inactiveRestaurant = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .isActive(false)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .build();

        entityManager.persist(activeRestaurant);
        entityManager.persist(inactiveRestaurant);

        Product validProduct = Product.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .isAvailable(true)
            .restaurant(activeRestaurant)
            .ingredients(null)
            .allergens(null)
            .build();

        Product unavailableProduct = Product.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .isAvailable(false)
            .restaurant(activeRestaurant)
            .ingredients(null)
            .allergens(null)
            .build();

        Product inactiveRestaurantProduct = Product.builder()
            .name(TestConstants.VALID_PRODUCT_NAME)
            .price(BigDecimal.valueOf(TestConstants.VALID_PRICE))
            .isAvailable(true)
            .restaurant(inactiveRestaurant)
            .ingredients(null)
            .allergens(null)
            .build();

        entityManager.persist(validProduct);
        entityManager.persist(unavailableProduct);
        entityManager.persist(inactiveRestaurantProduct);

        entityManager.flush();

        List<Product> result = productRepository.searchAvailableProductsByName(TestConstants.VALID_PRODUCT_NAME);

        assertEquals(1, result.size());
        assertEquals(TestConstants.VALID_PRODUCT_NAME, result.getFirst().getName());
    }
}
