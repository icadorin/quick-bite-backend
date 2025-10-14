package com.quickbite.product_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.exception.ResourceNotFoundException;
import com.quickbite.product_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ObjectMapper objectMapper;

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
        Restaurant restaurant = Restaurant.builder()
            .ownerId(request.getOwnerId())
            .name(request.getName())
            .description(request.getDescription())
            .address(convertToJson(request.getAddress()))
            .phone(request.getPhone())
            .email(request.getEmail())
            .logoUrl(request.getLogoUrl())
            .bannerUrl(request.getBannerUrl())
            .cuisineType(request.getCuisineType())
            .isActive(request.getIsActive())
            .openingHours(convertToJson(request.getOpeningHours()))
            .deliveryTimeRange(request.getDeliveryTimeRange())
            .minimumOrderAmount(request.getMinimumOrderAmount())
            .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }

    public List<RestaurantResponse> searchRestaurants(String name) {
        return restaurantRepository.searchActiveRestaurantsByName(name)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeAndIsActiveTrue(cuisineType)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<RestaurantResponse> getRestaurantsWithMinRating(Double minRating) {
        return restaurantRepository.findActiveRestaurantsWithMinRating(minRating)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = objectMapper.convertValue(restaurant, RestaurantResponse.class);

        try {
            if (restaurant.getAddress() != null) {
                response.setAddress(objectMapper.readValue(
                    restaurant.getAddress(),
                    new TypeReference<Map<String, Object>>() {}
                ));
            }

            if (restaurant.getOpeningHours() != null) {
                response.setOpeningHours(objectMapper.readValue(
                    restaurant.getOpeningHours(),
                    new TypeReference<Map<String, String>>() {}
                ));
            }
        } catch (Exception e) {
            //
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
