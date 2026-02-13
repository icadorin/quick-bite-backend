package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.constants.TestConstants;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.mapper.RestaurantCreateMapper;
import com.quickbite.product_service.mapper.RestaurantPatchMapper;
import com.quickbite.product_service.mapper.RestaurantResponseMapper;
import com.quickbite.product_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantRepository repository;

    @Mock
    private RestaurantCreateMapper createMapper;

    @Mock
    private RestaurantPatchMapper patchMapper;

    @Mock
    private RestaurantResponseMapper responseMapper;

    @InjectMocks
    private RestaurantService service;

    private RestaurantRequest validRequest;
    private Restaurant activeRestaurant;
    private RestaurantResponse response;

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

        response = RestaurantResponse.builder()
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
        when(repository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
            null
        )).thenReturn(false);

        when(createMapper.toEntity(validRequest))
            .thenReturn(activeRestaurant);
        when(repository.save(activeRestaurant))
            .thenReturn(activeRestaurant);
        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        RestaurantResponse result = service.createRestaurant(validRequest);

        assertEquals(response, result);

        verify(repository).save(activeRestaurant);
    }

    @Test
    void createRestaurant_shouldThrow_whenNameAlreadyExists() {
        when(repository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
        null
        )).thenReturn(true);

        BusinessRuleViolationException ex = assertThrows(
            BusinessRuleViolationException.class,
            () -> service.createRestaurant(validRequest)
        );

        assertEquals(
            TestConstants.RESTAURANT_NAME_ALREADY_EXISTS_MESSAGE,
            ex.getMessage()
        );

        verify(repository, never()).save(any());
        verify(createMapper, never()).toEntity(any());
        verify(responseMapper, never()).toResponse(any());
    }

    @Test
    void getRestaurantById_shouldReturn_whenExists() {
        when(repository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.getRestaurantById(TestConstants.VALID_RESTAURANT_ID);

        assertEquals(response, result);
    }

    @Test
    void getRestaurantById_shouldThrow_whenNotFound() {
        when(repository.findByIdAndIsActiveTrue(TestConstants.NON_EXISTENT_ID))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.getRestaurantById(TestConstants.NON_EXISTENT_ID)
        );
    }

    @Test
    void getRestaurantById_shouldThrow_whenIsInvalid() {
        assertThrows(
            DataValidationException.class,
            () -> service.getRestaurantById(0L)
        );
    }

    @Test
    void getRestaurantEntity_shouldReturnEntity() {
        when(repository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));

        var result = service.getRestaurantEntity(TestConstants.VALID_RESTAURANT_ID);

        assertEquals(activeRestaurant, result);
    }

    @Test
    void getRestaurants_shouldReturnFilteredRestaurants() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        Page<RestaurantResponse> result =
            service.getRestaurants(new RestaurantFilter(
                null,
                null,
                null,
                null,
                true
            ), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchRestaurants_shouldReturnResults() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.searchRestaurants(TestConstants.SEARCH_TERM_PIZZA, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchRestaurants_shouldThrow_whenBlank() {
        assertThrows(
            DataValidationException.class,
            () -> service.searchRestaurants(" ", PageRequest.of(0, 10))
        );
    }

    @Test
    void getRestaurantsByCuisine_shouldReturnResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.getRestaurantsByCuisine(
            TestConstants.VALID_CUISINE_TYPE,
            pageable
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRestaurantsWithMinRating_shouldReturnResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.getRestaurantsWithMinRating(4.0, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRestaurantsWithMinRating_shouldThrow_whenInvalid() {
        assertThrows(
            DataValidationException.class,
            () -> service.getRestaurantsWithMinRating(-1.0, PageRequest.of(0, 10))
        );
    }

    @Test
    void getRestaurants_shouldUseDefaultFilter_whenNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.getRestaurants(null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRestaurantsByOwner_shouldReturnRestaurants_whenOwnerExists() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Restaurant> mockPage = new PageImpl<>(List.of(activeRestaurant));

        when(repository.findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable))
        ).thenReturn(mockPage);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        Page<RestaurantResponse> result =
            service.getRestaurantsByOwner(TestConstants.VALID_OWNER_ID, pageable);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());

        verify(repository).findAll(
            ArgumentMatchers.<Specification<Restaurant>>any(),
            eq(pageable)
        );
    }

    @Test
    void getRestaurantByOwner_shouldThrow_whenOwnerIdInvalid() {
        Long invalidOwnerId = TestConstants.INVALID_ID;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        DataValidationException exception = assertThrows(
            DataValidationException.class,
            () -> service.getRestaurantsByOwner(invalidOwnerId, pageable)
        );

        assertEquals(TestConstants.INVALID_OWNER_ID_MESSAGE, exception.getMessage());

        verifyNoMoreInteractions(repository);
    }

    @Test
    void updateRestaurant_shouldUpdateSuccessfully() {
        when(repository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));
        when(repository.existsByNameAndOwnerIdAndIdNot(
            TestConstants.VALID_RESTAURANT_NAME,
            TestConstants.VALID_OWNER_ID,
            TestConstants.VALID_RESTAURANT_ID
        )).thenReturn(false);

        doNothing().when(patchMapper)
            .updateRestaurantFromRequest(validRequest, activeRestaurant);

        when(repository.save(activeRestaurant))
            .thenReturn(activeRestaurant);

        when(responseMapper.toResponse(activeRestaurant))
            .thenReturn(response);

        var result = service.updateRestaurant(
            TestConstants.VALID_RESTAURANT_ID,
            validRequest
        );

        assertEquals(response, result);
    }

    @Test
    void updateRestaurant_shouldThrow_whenDuplicateName() {
         when(repository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
             .thenReturn(Optional.of(activeRestaurant));

         when(repository.existsByNameAndOwnerIdAndIdNot(any(), any(), any()))
             .thenReturn(true);

         assertThrows(
             BusinessRuleViolationException.class,
             () -> service.updateRestaurant(
                 TestConstants.VALID_RESTAURANT_ID,
                 validRequest
             )
         );
    }

    @Test
    void deleteRestaurant_shouldDeleteSuccessfully() {
        when(repository.findByIdAndIsActiveTrue(TestConstants.VALID_RESTAURANT_ID))
            .thenReturn(Optional.of(activeRestaurant));

        service.deleteRestaurant(TestConstants.VALID_RESTAURANT_ID);

        assertFalse(activeRestaurant.getIsActive());
        verify(repository).save(activeRestaurant);
    }
}
