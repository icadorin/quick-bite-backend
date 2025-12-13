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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCreateMapper restaurantCreateMapper;
    private final RestaurantPatchMapper restaurantPatchMapper;
    private final RestaurantResponseMapper restaurantResponseMapper;

    public List<RestaurantResponse> getAllActiveRestaurant() {
        return restaurantResponseMapper.toResponseList(
            restaurantRepository.findByIsActiveTrue()
        );
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        return restaurantResponseMapper.toResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurantByOwner(Long ownerId) {
        return restaurantResponseMapper.toResponseList(
            restaurantRepository.findByOwnerId(ownerId)
        );
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {

        if (restaurantRepository.existsByNameAndOwnerId(request.getName().trim(), request.getOwnerId())) {
            throw new BusinessRuleViolationException("Restaurant with name '" + request.getName() +
                "' already exists for this owner");
        }

        Restaurant restaurant = restaurantCreateMapper.toEntity(request);
        restaurant.setIsActive(true);
        restaurant.setName(request.getName().trim());

        return restaurantResponseMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (restaurantRepository.existsByNameAndOwnerId(request.getName().trim(), request.getOwnerId())) {
            throw new BusinessRuleViolationException("Restaurant name already exists for this owner");
        }

        restaurantPatchMapper.updateRestaurantFromRequest(request, restaurant);

        return restaurantResponseMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    public List<RestaurantResponse> searchRestaurants(String name) {
        if (!StringUtils.hasText(name) || name.trim().length() < 2) {
            throw new DataValidationException("Search term must be at least 2 characters long");
        }

        try {
            return restaurantResponseMapper.toResponseList(
                restaurantRepository.searchActiveRestaurantsByName(name)
            );
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error searching restaurants", e);
        }
    }

    public List<RestaurantResponse> getRestaurantsCuisine(String cuisineType) {
        if (!StringUtils.hasText(cuisineType)) {
            throw new DataValidationException("Cuisine type is required");
        }

        try {
            return restaurantResponseMapper.toResponseList(
                restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType)
            );
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving restaurants by cuisine type");
        }
    }

    public List<RestaurantResponse> getRestaurantsWithMinRating(Double minRating) {
        if (minRating == null || minRating < 0 || minRating > 5) {
            throw new DataValidationException("Minimum rating must be between 0 and 5");
        }

        try {
            return restaurantResponseMapper.toResponseList(
                restaurantRepository.findActiveRestaurantsWithMinRating(minRating)
            );
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving restaurants by minimum rating", e);
        }
    }

    public Restaurant getRestaurantEntity(Long id) {
        return restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }
}
