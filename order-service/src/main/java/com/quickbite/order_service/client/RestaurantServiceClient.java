package com.quickbite.order_service.client;

import com.quickbite.order_service.dtos.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface RestaurantServiceClient {

    @GetMapping("/api/restaurants/{id}")
    RestaurantResponse getRestaurant(@PathVariable Long id);

    @GetMapping("/api/restaurants/{id}/exists")
    Boolean validateRestaurant(@PathVariable Long id);
}
