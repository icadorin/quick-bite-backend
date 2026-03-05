package com.quickbite.order_service.client.aggregator;

import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.dto.RestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalOrderDataLoader {

    private final ProductServiceClient productClient;

    public Map<Long, ProductResponse> loadProducts(List<Long> productIds) {

        return productIds.stream()
            .distinct()
            .collect(Collectors.toMap(
                id -> id,
                productClient::getProduct
            ));
    }

    public Map<Long, RestaurantResponse> loadRestaurants(List<Long> restaurantIds) {

        return restaurantIds.stream()
            .distinct()
            .collect(Collectors.toMap(
                id -> id,
                productClient::getRestaurant
            ));
    }
}
