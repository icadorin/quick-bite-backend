package com.quickbite.product_service.controller;

import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllActiveRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllActiveRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(
        @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantByOwner(
        @PathVariable @Positive Long ownerId
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByOwner(ownerId));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
        @Valid @RequestBody RestaurantRequest request
    ) {
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
        @PathVariable @Positive Long id,
        @Valid @RequestBody RestaurantRequest request
    ) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
        @PathVariable @Positive Long id
    ) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
        @RequestParam String name
    ) {
        return ResponseEntity.ok(restaurantService.searchRestaurants(name));
    }

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantByCuisine(
        @PathVariable String cuisineType
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisine(cuisineType));
    }

    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantWithMinRating(
        @PathVariable @PositiveOrZero Double minRating
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurantsWithMinRating(minRating));
    }
}
