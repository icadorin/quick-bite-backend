package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantCreateMapper createMapper;

    @Mock
    private RestaurantPatchMapper patchMapper;

    @Mock
    private RestaurantResponseMapper responseMapper;

    @InjectMocks
    private RestaurantService restaurantService;

    private RestaurantRequest validRequest;
    private Restaurant activeRestaurant;
    private RestaurantResponse restaurantResponse;

    @BeforeEach
    void setUp() {

        validRequest = RestaurantRequest.builder()
            .ownerId(TestConstants.VALID_OWNER_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
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
            .build();

        restaurantResponse = RestaurantResponse.builder()
            .id(TestConstants.VALID_RESTAURANT_ID)
            .name(TestConstants.VALID_RESTAURANT_NAME)
            .description(TestConstants.VALID_DESCRIPTION)
            .email(TestConstants.VALID_EMAIL)
            .phone(TestConstants.VALID_PHONE)
            .cuisineType(TestConstants.VALID_CUISINE_TYPE)
            .build();
    }

    @Test
    void createRestaurant_shouldCreateSuccessfully() {
        when(restaurantRepository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
            null
        )).thenReturn(false);

        when(createMapper.toEntity(validRequest))
            .thenReturn(activeRestaurant);
        when(restaurantRepository.save(activeRestaurant))
            .thenReturn(activeRestaurant);
        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(restaurantResponse);

        RestaurantResponse result =
            restaurantService.createRestaurant(validRequest);

        assertEquals(restaurantResponse, result);

        verify(createMapper).toEntity(validRequest);
        verify(restaurantRepository).save(activeRestaurant);
        verify(responseMapper).toResponse(activeRestaurant);
    }

    @Test
    void createRestaurant_shouldThrow_whenNameAlreadyExists() {
        when(restaurantRepository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
        null
        )).thenReturn(true);

        BusinessRuleViolationException ex = assertThrows(
            BusinessRuleViolationException.class,
            () -> restaurantService.createRestaurant(validRequest)
        );

        assertEquals(
            "Restaurant name already exists for this owner",
            ex.getMessage()
        );

        verify(restaurantRepository, never()).save(any());
        verify(createMapper, never()).toEntity(any());
        verify(responseMapper, never()).toResponse(any());
    }

    @Test
    void getRestaurantById_shouldReturn_whenExists() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(restaurantResponse);

        RestaurantResponse result =
            restaurantService.getRestaurantById(TestConstants.VALID_RESTAURANT_ID);

        assertEquals(restaurantResponse, result);

        verify(restaurantRepository)
            .findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID);
        verify(responseMapper).toResponse(activeRestaurant);
    }

    @Test
    void getRestaurantById_shouldThrow_whenNotFound() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> restaurantService.getRestaurantById(TestConstants.NON_EXISTENT_ID)
        );

        verify(responseMapper, never()).toResponse(any());
    }

    @Test
    void getRestaurantByOwner_shouldReturnRestaurants_whenOwnerExists() {
        when(restaurantRepository.findByOwnerIdAndIsActiveTrue(TestConstants.VALID_OWNER_ID))
            .thenReturn(List.of(activeRestaurant));
        when(responseMapper.toResponseList(List.of(activeRestaurant)))
            .thenReturn(List.of(restaurantResponse));

        List<RestaurantResponse> result =
            restaurantService.getRestaurantsByOwner(TestConstants.VALID_OWNER_ID);

        assertEquals(List.of(restaurantResponse), result);

        verify(restaurantRepository)
            .findByOwnerIdAndIsActiveTrue(TestConstants.VALID_OWNER_ID);
        verify(responseMapper)
            .toResponseList(List.of(activeRestaurant));
    }

    @Test
    void updateRestaurant_shouldUpdateSuccessfully() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(restaurantRepository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
            TestConstants.VALID_RESTAURANT_ID
        )).thenReturn(false);

        doNothing().when(patchMapper)
            .updateRestaurantFromRequest(validRequest, activeRestaurant);

        when(restaurantRepository.save(activeRestaurant))
            .thenReturn(activeRestaurant);
        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(restaurantResponse);

        RestaurantResponse result =
            restaurantService.updateRestaurant(
                TestConstants.VALID_RESTAURANT_ID,
                validRequest
            );

        assertEquals(restaurantResponse, result);

        verify(restaurantRepository)
            .findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID);
        verify(patchMapper)
            .updateRestaurantFromRequest(validRequest, activeRestaurant);
        verify(restaurantRepository).save(activeRestaurant);
        verify(responseMapper).toResponse(activeRestaurant);
    }

    @Test
    void deleteRestaurant_shouldDeleteSuccessfully() {
        when(restaurantRepository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));

        restaurantService.deleteRestaurant(TestConstants.VALID_RESTAURANT_ID);

        assertFalse(activeRestaurant.getIsActive());

        verify(restaurantRepository)
            .findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID);
        verify(restaurantRepository).save(activeRestaurant);
    }
}
