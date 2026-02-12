package com.quickbite.product_service.controller;

import com.quickbite.product_service.constants.ApiPaths;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.RESTAURANTS)
@RequiredArgsConstructor
@Validated
public class RestaurantController {

    private final RestaurantService service;

    @GetMapping
    public Page<RestaurantResponse> getRestaurants(
        RestaurantFilter filter,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return service.getRestaurants(filter, pageable);
    }

    @GetMapping(ApiPaths.BY_ID)
    public RestaurantResponse getById(@PathVariable("id") @Positive Long id) {
        return service.getRestaurantById(id);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public Page<RestaurantResponse> getByOwner(
        @PathVariable("ownerId") @Positive Long ownerId,
        @PageableDefault(sort = "name",
            direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getRestaurantsByOwner(ownerId, pageable);
    }

    @GetMapping("/search")
    public Page<RestaurantResponse> search(
        @RequestParam @Size(min = 2) String name,
        @PageableDefault(size = 20, sort = "name",
            direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.searchRestaurants(name, pageable);
    }

    @GetMapping("/cuisine/{cuisineType}")
    public Page<RestaurantResponse> getByCuisine(
        @PathVariable String cuisineType,
        @PageableDefault(size = 20, sort = "name",
            direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getRestaurantsByCuisine(cuisineType, pageable);
    }

    @GetMapping("/rating")
    public Page<RestaurantResponse> getByCuisineAndType(
        @RequestParam(value = "min", defaultValue = "0.0") @PositiveOrZero Double minRating,
        @PageableDefault(size = 20, sort = "averageRating",
            direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.getRestaurantsWithMinRating(minRating, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse create(@Valid @RequestBody RestaurantRequest request) {
        return service.createRestaurant(request);
    }

    @PutMapping(ApiPaths.BY_ID)
    @PreAuthorize("@restaurantSecurity.canManageRestaurant(#id)")
    public RestaurantResponse update(
        @PathVariable @Positive Long id,
        @Valid @RequestBody RestaurantRequest request
    ) {
        return service.updateRestaurant(id, request);
    }

    @DeleteMapping(ApiPaths.BY_ID)
    @PreAuthorize("@restaurantSecurity.canManageRestaurant(#id)")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        service.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
