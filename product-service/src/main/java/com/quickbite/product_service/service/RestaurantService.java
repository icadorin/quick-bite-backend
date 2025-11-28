package com.quickbite.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.exception.BusinessRuleViolationException;
import com.quickbite.product_service.exception.DataValidationException;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ObjectMapper objectMapper;

    public void validateRestaurantRequest(RestaurantRequest request) {
        if (request.getOwnerId() == null || request.getOwnerId() <= 0) {
            throw new DataValidationException("Valid owner ID is required");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new DataValidationException("Restaurant name is required");
        }

        if (request.getName().length() > 255) {
            throw new DataValidationException("Restaurant name must not exceed 255 characters");
        }

        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            throw new DataValidationException("Description must not exceed 1000 characters");
        }

        if (request.getEmail() != null) {
            if (request.getEmail().length() > 255) {
                throw new DataValidationException("Email must not exceed 255 characters");
            }

            if (!isValidEmail(request.getEmail())) {
                throw new DataValidationException("Email must be valid");
            }
        }

        if (request.getPhone() != null) {
            if (request.getPhone().length() > 20) {
                throw new DataValidationException("Phone must not exceed 20 characters");
            }

            if (!isValidPhone(request.getPhone())) {
                throw new DataValidationException("Phone number must be valid");
            }
        }

        if (request.getLogoUrl() != null && request.getLogoUrl().length() > 500) {
            throw new DataValidationException("Banner URL must not exceed 500 characters");
        }

        if (request.getBannerUrl() != null && request.getBannerUrl().length() > 500) {
            throw new DataValidationException("Banner URL must not exceed 500 characters");
        }

        if (request.getCuisineType() != null && request.getCuisineType().length() > 100) {
            throw new DataValidationException("Cuisine type must not exceed 100 characters");
        }

        if (request.getDeliveryTimeRange() != null && request.getDeliveryTimeRange().length() > 20) {
            throw new DataValidationException("Delivery time range must not exceed 20 characters");
        }

        if (request.getMinimumOrderAmount() != null && request.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DataValidationException("Minimum order amount must be zero or positive");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^[+]?[1-9]\\d{0,15}$";
        return phone.matches(phoneRegex);
    }

    public List<RestaurantResponse> getAllActiveRestaurant() {
        return restaurantRepository.findByIsActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        return mapToResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurantByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        validateRestaurantRequest(request);

        if (restaurantRepository.existsByNameAndOwnerId(request.getName().trim(), request.getOwnerId())) {
            throw new BusinessRuleViolationException("Restaurant with name '" + request.getName() +
                "' already exists for this owner");
        }

        try {
            Restaurant restaurant = Restaurant.builder()
                .ownerId(request.getOwnerId())
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .cuisineType(request.getCuisineType())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .openingHours(request.getOpeningHours())
                .deliveryTimeRange(request.getDeliveryTimeRange())
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ?
                    request.getMinimumOrderAmount() : BigDecimal.ZERO)
                .build();

            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            return mapToResponse(savedRestaurant);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error creating restaurant", e);
        }
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid restaurant ID");
        }

        validateRestaurantRequest(request);

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        String name = request.getName().trim();

        if (!request.getName().equals(name)) {
            throw new BusinessRuleViolationException("Restaurant name cannot contain leading or trailing spaces");
        }

        if (restaurantRepository.existsByNameAndOwnerId(name, request.getOwnerId())) {
            throw new BusinessRuleViolationException(
                "Restaurant with name '" + request.getName() + "' already exists for this owner"
            );
        }

        try {
            restaurant.setName(request.getName().trim());
            restaurant.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            restaurant.setAddress(request.getAddress());
            restaurant.setPhone(request.getPhone());
            restaurant.setEmail(request.getEmail());
            restaurant.setLogoUrl(request.getLogoUrl());
            restaurant.setBannerUrl(request.getBannerUrl());
            restaurant.setCuisineType(request.getCuisineType());
            restaurant.setIsActive(request.getIsActive());
            restaurant.setOpeningHours(request.getOpeningHours());
            restaurant.setDeliveryTimeRange(request.getDeliveryTimeRange());
            restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount());

            if (request.getIsActive() != null) {
                restaurant.setIsActive(request.getIsActive());
            }

            restaurant.setOpeningHours(request.getOpeningHours());
            restaurant.setDeliveryTimeRange(request.getDeliveryTimeRange());

            if (request.getMinimumOrderAmount() != null) {
                restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount());
            }

            Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
            return mapToResponse(updatedRestaurant);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error updating restaurant", e);
        }
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid restaurant ID");
        }

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        try {
            restaurant.setIsActive(false);
            restaurantRepository.save(restaurant);
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error deleting restaurant", e);
        }
    }

    public List<RestaurantResponse> searchRestaurants(String name) {
        if (!StringUtils.hasText(name) || name.trim().length() < 2) {
            throw new DataValidationException("Search term must be at least 2 characters long");
        }

        try {
            return restaurantRepository.searchActiveRestaurantsByName(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error searching restaurants", e);
        }
    }

    public List<RestaurantResponse> getRestaurantsCuisine(String cuisineType) {
        if (!StringUtils.hasText(cuisineType)) {
            throw new DataValidationException("Cuisine type is required");
        }

        try {
            return restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving restaurants by cuisine type");
        }
    }

    public List<RestaurantResponse> getRestaurantsWithMinRating(Double minRating) {
        if (minRating == null || minRating < 0 || minRating > 5) {
            throw new DataValidationException("Minimum rating must be between  0 and 5");
        }

        try {
            return restaurantRepository.findActiveRestaurantsWithMinRating(minRating)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("Error retrieving restaurants by minimum rating", e);
        }
    }

    public Restaurant getRestaurantEntity(Long id) {
        return restaurantRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = objectMapper.convertValue(restaurant, RestaurantResponse.class);

        if (restaurant.getAddress() != null) {
            response.setAddress(restaurant.getAddress());
        }

        if (restaurant.getOpeningHours() != null) {
            response.setOpeningHours(restaurant.getOpeningHours());
        }

        return response;
    }

    public String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting object in json", e);
        }
    }
}
