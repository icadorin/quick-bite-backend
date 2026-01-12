package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.RESTAURANTS)
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantService.getAllActiveRestaurants();
    }

    @GetMapping(ApiPaths.BY_ID)
    public RestaurantResponse getRestaurantById(
        @PathVariable @Positive Long id
    ) {
        return restaurantService.getRestaurantById(id);
    }

    @GetMapping("/owner/{ownerId}")
    public List<RestaurantResponse> getRestaurantByOwner(
        @PathVariable @Positive Long ownerId
    ) {
        return restaurantService.getRestaurantsByOwner(ownerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(
        @Valid @RequestBody RestaurantRequest request
    ) {
        return restaurantService.createRestaurant(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    public RestaurantResponse updateRestaurant(
        @PathVariable @Positive Long id,
        @Valid @RequestBody RestaurantRequest request
    ) {
        return restaurantService.updateRestaurant(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(
        @PathVariable @Positive Long id
    ) {
        restaurantService.deleteRestaurant(id);
    }

    @GetMapping(ApiPaths.SEARCH)
    public List<RestaurantResponse> searchRestaurants(
        @RequestParam String name
    ) {
        return restaurantService.searchRestaurants(name);
    }

    @GetMapping("/cuisine/{cuisineType}")
    public List<RestaurantResponse> getRestaurantByCuisine(
        @PathVariable String cuisineType
    ) {
        return restaurantService.getRestaurantsByCuisine(cuisineType);
    }

    @GetMapping("/rating/{minRating}")
    public List<RestaurantResponse> getRestaurantWithMinRating(
        @PathVariable @PositiveOrZero Double minRating
    ) {
        return restaurantService.getRestaurantsWithMinRating(minRating);
    }
}
