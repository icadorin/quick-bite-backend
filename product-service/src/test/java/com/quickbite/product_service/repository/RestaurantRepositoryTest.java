package com.quickbite.product_service.repository;

import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.repository.specification.RestaurantSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnOnlyActiveRestaurants_whenFiltering() {
         entityManager.persist(
             Restaurant.builder()
                 .name(TestConstants.VALID_PRODUCT_NAME)
                 .ownerId(TestConstants.VALID_OWNER_ID)
                 .isActive(true)
                 .build()
         );

         entityManager.persist(
             Restaurant.builder()
                 .name(TestConstants.VALID_PRODUCT_NAME)
                 .ownerId(TestConstants.VALID_OWNER_ID)
                 .isActive(false)
                 .build()
         );

         entityManager.flush();

         var filter = new RestaurantFilter(
             TestConstants.VALID_PRODUCT_NAME,
             null,
             null,
             null,
             true
         );

         var result = restaurantRepository.findAll(
             RestaurantSpecification.withFilters(filter)
         );

         assertEquals(1, result.size());
    }
}
