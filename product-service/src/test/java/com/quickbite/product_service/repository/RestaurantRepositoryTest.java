package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.entity.Restaurant;
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
public class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdAndActiveTrue_shouldReturnRestaurant_whenExists() {
        Restaurant restaurant = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .isActive(true)
            .build();

        entityManager.persist(restaurant);
        entityManager.flush();

        Optional<Restaurant> result =
            restaurantRepository.findByIdAndIsActiveTrue(restaurant.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void findByOwnerIdAndIsActiveTrue_shouldReturnOnlyActiveRestaurants() {
        Restaurant active = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .isActive(true)
            .build();

        Restaurant inactive = Restaurant.builder()
            .name(TestConstants.UPDATED_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .isActive(false)
            .build();

        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();

        List<Restaurant> result =
            restaurantRepository.findByOwnerIdAndIsActiveTrue(
                TestConstants.VALID_OWNER_ID
            );

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getIsActive());
    }

    @Test
    void searchActiveRestaurantByName_shouldReturnMatchingRestaurants() {
        Restaurant restaurant = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .isActive(true)
            .build();

        entityManager.persist(restaurant);
        entityManager.flush();

        List<Restaurant> result =
            restaurantRepository.searchActiveRestaurantsByName(
                TestConstants.VALID_SEARCH_TERM
            );

        assertEquals(1, result.size());
    }

    @Test
    void existsByNameAndOwnerId_shouldReturnTrue_whenExists() {
        Restaurant restaurant = Restaurant.builder()
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .isActive(true)
            .build();

        entityManager.persist(restaurant);
        entityManager.flush();

        boolean exists =
            restaurantRepository.existsByNameAndOwnerId(
                TestConstants.VALID_RESTAURANT_NAME,
                TestConstants.VALID_OWNER_ID
            );

        assertTrue(exists);
    }
}
