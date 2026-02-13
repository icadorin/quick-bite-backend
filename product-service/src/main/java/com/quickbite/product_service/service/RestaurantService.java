package com.quickbite.product_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.core.exception.DataValidationException;
import com.quickbite.core.exception.ResourceNotFoundException;
import com.quickbite.product_service.dto.RestaurantRequest;
import com.quickbite.product_service.dto.RestaurantResponse;
import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.entity.Restaurant;
import com.quickbite.product_service.mapper.RestaurantCreateMapper;
import com.quickbite.product_service.mapper.RestaurantPatchMapper;
import com.quickbite.product_service.mapper.RestaurantResponseMapper;
import com.quickbite.product_service.repository.RestaurantRepository;
import com.quickbite.product_service.repository.specification.RestaurantSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class RestaurantService {

    private final RestaurantRepository repository;
    private final RestaurantCreateMapper createMapper;
    private final RestaurantPatchMapper patchMapper;
    private final RestaurantResponseMapper responseMapper;

    public Page<RestaurantResponse> getRestaurants(
        RestaurantFilter filter,
        Pageable pageable
    ) {
        var specification = RestaurantSpecification.withFilters(filter);

        return repository
            .findAll(specification, pageable)
            .map(responseMapper::toResponse);
    }

    public RestaurantResponse getRestaurantById(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = repository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: %d".formatted(id)
            ));

        return responseMapper.toResponse(restaurant);
    }

    public Restaurant getRestaurantEntity(Long id) {
        validateId(id, "restaurant");

        return repository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: %d".formatted(id)
            ));
    }

    public Page<RestaurantResponse> getRestaurantsByOwner(Long ownerId, Pageable pageable) {
        validateId(ownerId, "owner");

        var filter = new RestaurantFilter(
            null,
            ownerId,
            null,
            null,
            true
        );

        var spec = RestaurantSpecification.withFilters(filter);

        return repository.findAll(spec, pageable)
            .map(responseMapper::toResponse);
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        validateUniqueRestaurantName(request.getName(), request.getOwnerId(), null);

        Restaurant restaurant = createMapper.toEntity(request);
        restaurant.setIsActive(true);

        return responseMapper.toResponse(repository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        validateId(id, "restaurant");

        Restaurant restaurant = repository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: %d".formatted(id)
            ));

        validateUniqueRestaurantName(request.getName(), request.getOwnerId(), id);

        patchMapper.updateRestaurantFromRequest(request, restaurant);

        return responseMapper.toResponse(repository.save(restaurant));
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        validateId(id, "restaurant");

        Restaurant restaurant = repository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurant not found with id: %d".formatted(id)
            ));

        restaurant.setIsActive(false);
        repository.save(restaurant);
    }

    public Page<RestaurantResponse> searchRestaurants(String name, Pageable pageable) {
        validateRequiredText(name, "Search name");

        var filter = new RestaurantFilter(
            name.trim(),
            null,
            null,
            null,
            true
        );

        var spec = RestaurantSpecification.withFilters(filter);

        return repository.findAll(spec, pageable)
            .map(responseMapper::toResponse);
    }

    public Page<RestaurantResponse> getRestaurantsByCuisine(String cuisineType, Pageable pageable) {
        validateRequiredText(cuisineType, "Cuisine type");

        var filter = new RestaurantFilter(
            null,
            null,
            cuisineType,
            null,
            true
        );

        var spec = RestaurantSpecification.withFilters(filter);

        return repository.findAll(spec, pageable)
            .map(responseMapper::toResponse);
    }

    public Page<RestaurantResponse> getRestaurantsWithMinRating(Double minRating, Pageable pageable) {
        if (minRating == null || minRating < 0) {
            throw new DataValidationException("Minimum rating must be zero or positive");
        }

        var filter = new RestaurantFilter(
            null,
            null,
            null,
            minRating,
            true
        );

        var spec = RestaurantSpecification.withFilters(filter);

        return repository.findAll(spec, pageable)
            .map(responseMapper::toResponse);
    }

    private void validateUniqueRestaurantName(String name, Long ownerId, Long currentRestaurantId) {
        boolean exists = repository.existsByNameAndOwnerIdAndIdNot(name, ownerId, currentRestaurantId);

        if (exists) {
            throw new BusinessRuleViolationException(
                "Restaurant name already exists for this owner"
            );
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new DataValidationException("Invalid %s ID: %d".formatted(fieldName, id));
        }
    }

    private void validateRequiredText(String value, String fieldName) {

        if (value == null || value.trim().isEmpty()) {
            throw new DataValidationException("%s must not be blank".formatted(fieldName));
        }
    }
}
