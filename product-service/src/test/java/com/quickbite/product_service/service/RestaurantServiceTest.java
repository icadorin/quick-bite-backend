package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RestaurantService restaurantService;

    private RestaurantRequest validRestaurantRequest;
    private Restaurant activeRestaurant;
    private RestaurantResponse restaurantResponse;

    @BeforeEach
    void setUp() {
        validRestaurantRequest = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
            .isActive(true)
            .build();

        activeRestaurant = Restaurant.builder()
            .id(TestConstants.VALID_RESTAURANT_ID)
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
            .isActive(true)
            .rating(TestConstants.VALID_RATING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        restaurantResponse = RestaurantResponse.builder()
            .id(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
            .rating(TestConstants.VALID_RATING)
            .build();
    }

    @Test
    void createRestaurant_ShouldCreateRestaurantSuccessfully() {
        when(restaurantRepository.existsByNameAndOwnerId(TestConstants.VALID_RESTAURANT_NAME, TestConstants.VALID_OWNER_ID))
            .thenReturn(false);

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(activeRestaurant);
        when(objectMapper.convertValue(any(Restaurant.class), eq(RestaurantResponse.class)))
            .thenReturn(restaurantResponse);

        RestaurantResponse result = restaurantService.createRestaurant(validRestaurantRequest);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_RESTAURANT_NAME, result.getName());
        verify(restaurantRepository).existsByNameAndOwnerId(
            TestConstants.VALID_RESTAURANT_NAME, TestConstants.VALID_OWNER_ID);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void createRestaurant_ShouldThrowExceptionWhenRestaurantNameExists() {
        when(restaurantRepository.existsByNameAndOwnerId(
            TestConstants.VALID_RESTAURANT_NAME, TestConstants.VALID_OWNER_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
            () -> restaurantService.createRestaurant(validRestaurantRequest));

        String expectedMessage = String.format(TestConstants.RESTAURANT_ALREADY_EXISTS_MESSAGE,
            TestConstants.VALID_RESTAURANT_NAME);
        assertEquals(expectedMessage, exception.getMessage());
        verify(restaurantRepository).existsByNameAndOwnerId(TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID);
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void createRestaurant_ShouldThrowValidationExceptionWhenEmailIsInvalid() {
        RestaurantRequest invalidRequest = validRestaurantRequest.toBuilder()
            .email(TestConstants.INVALID_EMAIL)
            .build();

        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> restaurantService.createRestaurant(invalidRequest));

        assertEquals(TestConstants.INVALID_EMAIL_MESSAGE, exception.getMessage());
        verify(restaurantRepository, never()).existsByNameAndOwnerId(anyString(), any());
    }

    @Test
    void createRestaurant_ShouldThrowValidationExceptionWhenPhoneIsInvalid() {
        RestaurantRequest invalidRequest = validRestaurantRequest.toBuilder()
            .phone(TestConstants.INVALID_PHONE)
            .build();

        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> restaurantService.createRestaurant(validRestaurantRequest));

        assertEquals(TestConstants.INVALID_PHONE_MESSAGE, exception.getMessage());
        verify(restaurantRepository, never()).existsByNameAndOwnerId(anyString(), any());
    }

    @Test
    void getRestaurantById_ShouldReturnRestaurantWhenExists() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(objectMapper.convertValue(any(Restaurant.class), eq(RestaurantResponse.class)))
            .thenReturn(restaurantResponse);

        RestaurantResponse result = restaurantService.getRestaurantById(TestConstants.VALID_RESTAURANT_ID);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_RESTAURANT_ID, result.getId());
        verify(restaurantRepository).findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID);
    }

    @Test
    void getRestaurantById_ShouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> restaurantService.getRestaurantById(TestConstants.NON_EXISTENT_ID));

        assertEquals(TestConstants.RESTAURANT_NOT_FOUND_MESSAGE +
            TestConstants.NON_EXISTENT_ID, exception.getMessage());
        verify(restaurantRepository).findByIdAndIsActiveTrue(TestConstants.NON_EXISTENT_ID);
    }
}
