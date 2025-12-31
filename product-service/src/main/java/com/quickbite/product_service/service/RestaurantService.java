package com.quickbite.product_service.service;

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
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCreateMapper restaurantCreateMapper;
    private final RestaurantPatchMapper restaurantPatchMapper;
    private final RestaurantResponseMapper restaurantResponseMapper;

    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantResponseMapper.toResponseList(
            restaurantRepository.findByIsActiveTrue()
        );
    }

    public RestaurantResponse getRestaurantById(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));

        return restaurantResponseMapper.toResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerId) {
        validateId(ownerId, "owner");

        return restaurantResponseMapper.toResponseList(
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

        Restaurant restaurant = restaurantCreateMapper.toEntity(request);
        restaurant.setIsActive(true);

        return restaurantResponseMapper.toResponse(
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

        boolean nameExists = restaurantRepository
            .existsByNameAndOwnerIdAndIdNot(
                request.getName(),
                request.getOwnerId(),
                id
            );

        if (nameExists) {
            throw new BusinessRuleViolationException(
                "Restaurant name already exists for this owner"
            );
        }

        restaurantPatchMapper.updateRestaurantFromRequest(request, restaurant);

        return restaurantResponseMapper.toResponse(
            restaurantRepository.save(restaurant)
        );
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: " + id
            ));

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    public List<RestaurantResponse> searchRestaurants(String name) {
        return restaurantResponseMapper.toResponseList(
            restaurantRepository.searchActiveRestaurantsByName(name)
        );
    }

    public List<RestaurantResponse> getRestaurantsByCuisine(String cuisineType) {
        return restaurantResponseMapper.toResponseList(
            restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType)
        );
    }

    public List<RestaurantResponse> getRestaurantsWithMinRating(Double minRating) {
        return restaurantResponseMapper.toResponseList(
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
}
