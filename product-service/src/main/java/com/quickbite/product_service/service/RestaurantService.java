package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.mapper.RestaurantCreateMapper;
import com.quickbite.product_service.mapper.RestaurantPatchMapper;
import com.quickbite.product_service.mapper.RestaurantResponseMapper;
import com.quickbite.product_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCreateMapper createMapper;
    private final RestaurantPatchMapper patchMapper;
    private final RestaurantResponseMapper responseMapper;

    public List<RestaurantResponse> getAllActiveRestaurants() {
        return responseMapper.toResponseList(
            restaurantRepository.findByIsActiveTrue()
        );
    }

    public RestaurantResponse getRestaurantById(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));

        return responseMapper.toResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerId) {
        validateId(ownerId, "owner");

        return responseMapper.toResponseList(
            restaurantRepository.findByOwnerIdAndIsActiveTrue(ownerId)
        );
    }

    @Transactional
    public RestaurantResponse createRestaurant(@Valid RestaurantRequest request) {

        validateUniqueRestaurantName(
            request.getName(),
            request.getOwnerId(),
            null
        );

        Restaurant restaurant = createMapper.toEntity(request);
        restaurant.setIsActive(true);

        return responseMapper.toResponse(
            restaurantRepository.save(restaurant)
        );
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, @Valid RestaurantRequest request) {
        validateId(id, "restaurant");

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));

        validateUniqueRestaurantName(
            request.getName(),
            request.getOwnerId(),
            id
        );

        patchMapper.updateRestaurantFromRequest(request, restaurant);

        return responseMapper.toResponse(
            restaurantRepository.save(restaurant)
        );
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    public List<RestaurantResponse> searchRestaurants(String name) {
        validateRequiredText(name, "Search name");

        return responseMapper.toResponseList(
            restaurantRepository.searchActiveRestaurantsByName(name)
        );
    }

    public List<RestaurantResponse> getRestaurantsByCuisine(String cuisineType) {
        validateRequiredText(cuisineType, "Cuisine type");

        return responseMapper.toResponseList(
            restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType)
        );
    }

    public List<RestaurantResponse> getRestaurantsWithMinRating(Double minRating) {
        if (minRating == null || minRating < 0) {
            throw new DataValidationException("Minimum rating must be zero or positive");
        }

        return responseMapper.toResponseList(
            restaurantRepository.findActiveRestaurantsWithMinRating(minRating)
        );
    }

    public Restaurant getRestaurantEntity(Long id) {
        validateId(id, "restaurant");

        return restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));
    }

    private void validateUniqueRestaurantName(
        String name,
        Long ownerId,
        Long currentRestaurantId
    ) {
        boolean exists = restaurantRepository
            .existsByNameAndOwnerIdAndIdNot(
                name,
                ownerId,
                currentRestaurantId
            );

        if (exists) {
            throw new BusinessRuleViolationException(
                "Restaurant name already exists for this owner"
            );
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new DataValidationException(
                "Invalid " + fieldName + " ID"
            );
        }
    }

    private void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DataValidationException(
                fieldName + " must not be blank"
            );
        }
    }
}
