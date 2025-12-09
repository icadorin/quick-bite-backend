package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.mapper.RestaurantCreateMapper;
import com.quickbite.product_service.mapper.RestaurantPatchMapper;
import com.quickbite.product_service.mapper.RestaurantResponseMapper;
import com.quickbite.product_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

    @Mock
    private RestaurantCreateMapper restaurantCreateMapper;

    @Mock
    private RestaurantResponseMapper restaurantResponseMapper;

    @Mock
    private RestaurantPatchMapper restaurantPatchMapper;

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

        when(restaurantCreateMapper.toEntity(validRestaurantRequest)).thenReturn(activeRestaurant);
        when(restaurantResponseMapper.toResponse(activeRestaurant)).thenReturn(restaurantResponse);
        when(restaurantRepository.save(any(Restaurant.class)))
            .thenReturn(activeRestaurant).thenReturn(activeRestaurant);

        RestaurantResponse result = restaurantService.createRestaurant(validRestaurantRequest);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_RESTAURANT_NAME, result.getName());
        verify(restaurantRepository).existsByNameAndOwnerId(
            TestConstants.VALID_RESTAURANT_NAME, TestConstants.VALID_OWNER_ID);
        verify(restaurantRepository).save(any(Restaurant.class));
        verify(restaurantCreateMapper).toEntity(validRestaurantRequest);
        verify(restaurantResponseMapper).toResponse(activeRestaurant);
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
            () -> restaurantService.createRestaurant(invalidRequest));

        assertEquals(TestConstants.INVALID_PHONE_MESSAGE, exception.getMessage());
        verify(restaurantRepository, never()).existsByNameAndOwnerId(anyString(), any());
    }

    @Test
    void createRestaurant_ShouldCreateRestaurantWithVariousCuisineTypes() {
       List<String> cuisineTypes = Arrays.asList(
           TestConstants.VALID_CUISINE_TYPE,
           TestConstants.VALID_CUISINE_TYPE_2,
           TestConstants.VALID_CUISINE_TYPE_3
       );

       for (String cuisineType : cuisineTypes) {
           RestaurantRequest request = validRestaurantRequest.toBuilder()
               .cuisineType(cuisineType)
               .build();

           Restaurant restaurantWithCuisine = activeRestaurant.toBuilder()
               .cuisineType(cuisineType)
               .build();

           RestaurantResponse responseWithCuisine = restaurantResponse.toBuilder()
               .cuisineType(cuisineType)
               .build();

           when(restaurantRepository.existsByNameAndOwnerId(anyString(), anyLong()))
               .thenReturn(false);
           when(restaurantCreateMapper.toEntity(request)).thenReturn(restaurantWithCuisine);
           when(restaurantResponseMapper.toResponse(restaurantWithCuisine)).thenReturn(responseWithCuisine);
           when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurantWithCuisine);

           RestaurantResponse result = restaurantService.createRestaurant(request);

           assertNotNull(result);
           assertEquals(cuisineType, result.getCuisineType());

           reset(restaurantRepository, restaurantCreateMapper, restaurantResponseMapper);
       }
    }

    @Test
    void createRestaurant_ShouldThrowExceptionWhenCuisineTypeExceedsMaxLength() {
        RestaurantRequest invalidRequest = validRestaurantRequest.toBuilder()
            .cuisineType(TestConstants.INVALID_CUISINE_TYPE )
            .build();

        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> restaurantService.createRestaurant(invalidRequest));

        assertEquals(TestConstants.CUISINE_TOO_LONG_MESSAGE, exception.getMessage());
    }

    @Test
    void createRestaurant_ShouldAcceptValidCuisineTypeLength() {
        RestaurantRequest request = validRestaurantRequest.toBuilder()
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
                .build();

        Restaurant restaurantToSave = activeRestaurant.toBuilder()
            .cuisineType(TestConstants.VALID_CUISINE_100)
            .build();

        RestaurantResponse expectedResponse = restaurantResponse.toBuilder()
            .cuisineType(TestConstants.VALID_CUISINE_100)
            .build();

        when(restaurantRepository.existsByNameAndOwnerId(anyString(), anyLong()))
            .thenReturn(false);
        when(restaurantCreateMapper.toEntity(request)).thenReturn(restaurantToSave);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurantToSave);
        when(restaurantResponseMapper.toResponse(restaurantToSave)).thenReturn(expectedResponse);

        RestaurantResponse result = restaurantService.createRestaurant(request);

        assertNotNull(result);
        assertNotNull(result.getCuisineType());
    }

    @Test
    void getRestaurantByCuisine_ShouldReturnFilteredRestaurants() {
        String cuisineType = TestConstants.VALID_CUISINE_TYPE;

        List<Restaurant> restaurants = List.of(
            activeRestaurant,
            activeRestaurant.toBuilder()
                .id(TestConstants.VALID_RESTAURANT_ID_2)
                .name(TestConstants.VALID_RESTAURANT_NAME_2)
                .cuisineType(cuisineType)
                .build()
        );

        List<RestaurantResponse> restaurantResponses = List.of(
            restaurantResponse,
            restaurantResponse.toBuilder()
                .id(TestConstants.VALID_RESTAURANT_ID_2)
                .name(TestConstants.VALID_RESTAURANT_NAME_2)
                .cuisineType(cuisineType)
                .build()
        );

        when(restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType))
            .thenReturn(restaurants);
        when(restaurantResponseMapper.toResponseList(restaurants)).thenReturn(restaurantResponses);

        List<RestaurantResponse> result = restaurantService.getRestaurantsCuisine(cuisineType);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(cuisineType, result.get(0).getCuisineType());
        assertEquals(cuisineType, result.get(1).getCuisineType());

        verify(restaurantRepository).findByCuisineTypeAndIsActiveTrue(cuisineType);
        verify(restaurantResponseMapper).toResponseList(restaurants);
    }

    @Test
    void getRestaurantById_ShouldReturnRestaurantWhenExists() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(restaurantResponseMapper.toResponse(activeRestaurant)).thenReturn(restaurantResponse);

        RestaurantResponse result = restaurantService.getRestaurantById(TestConstants.VALID_RESTAURANT_ID);

        assertNotNull(result);
        assertEquals(TestConstants.VALID_RESTAURANT_ID, result.getId());
        verify(restaurantRepository).findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID);
        verify(restaurantResponseMapper).toResponse(activeRestaurant);
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

    @Test
    void getRestaurantsWithMinRating_ShouldRestaurantsWhenValidRating() {
        List<Restaurant> restaurants = List.of(activeRestaurant);
        List<RestaurantResponse> restaurantResponses = List.of(restaurantResponse);

        when(restaurantRepository.findActiveRestaurantsWithMinRating(TestConstants.VALID_RATING))
            .thenReturn(restaurants);
        when(restaurantResponseMapper.toResponseList(restaurants)).thenReturn(restaurantResponses);

        List<RestaurantResponse> result = restaurantService.getRestaurantsWithMinRating(TestConstants.VALID_RATING);

        assertFalse(result.isEmpty());
        verify(restaurantRepository).findActiveRestaurantsWithMinRating(TestConstants.VALID_RATING);
        verify(restaurantResponseMapper).toResponseList(restaurants);
    }

    @Test
    void getRestaurantWithMinRating_ShouldRestaurantWhenInvalidRating() {
        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> restaurantService.getRestaurantsWithMinRating(TestConstants.INVALID_RATING));

        assertEquals(TestConstants.INVALID_RATING_MESSAGE, exception.getMessage());
        verify(restaurantRepository, never()).findActiveRestaurantsWithMinRating(anyDouble());
    }

    @Test
    void deleteRestaurant_ShouldSoftDeleteRestaurantSuccessfully() {
        when(restaurantRepository.findById(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(activeRestaurant);

        restaurantService.deleteRestaurant(TestConstants.VALID_RESTAURANT_ID);

        assertFalse(activeRestaurant.getIsActive());
        verify(restaurantRepository).findById(TestConstants.VALID_RESTAURANT_ID);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void searchRestaurants_ShouldReturnRestaurantsWhenValidSearchTerm() {
        List<Restaurant> restaurants = List.of(activeRestaurant);
        List<RestaurantResponse> restaurantResponses = List.of(restaurantResponse);

        when(restaurantRepository.searchActiveRestaurantsByName(TestConstants.VALID_SEARCH_TERM))
            .thenReturn(restaurants);
        when(restaurantResponseMapper.toResponseList(restaurants)).thenReturn(restaurantResponses);

        List<RestaurantResponse> result = restaurantService.searchRestaurants(TestConstants.VALID_SEARCH_TERM);

        assertFalse(result.isEmpty());
        verify(restaurantRepository).searchActiveRestaurantsByName(TestConstants.VALID_SEARCH_TERM);
        verify(restaurantResponseMapper).toResponseList(restaurants);
    }

    @Test
    void searchRestaurants_ShouldThrowExceptionWhenSearchTermTooShort() {
        DataValidationException exception = assertThrows(DataValidationException.class,
            () -> restaurantService.searchRestaurants(TestConstants.SEARCH_TERM_SHORT));

        assertEquals(TestConstants.SEARCH_TERM_TOO_SHORT_MESSAGE, exception.getMessage());
        verify(restaurantRepository, never()).searchActiveRestaurantsByName(anyString());
    }
}
