package com.quickbite.order_service.client;

import com.quickbite.order_service.dtos.ProductResponse;
import com.quickbite.order_service.dtos.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/products/{id}/exists")
    Boolean validateProduct(@PathVariable("id") Long id);

    @GetMapping("/api/restaurants/{id}")
    RestaurantResponse getRestaurant(@PathVariable("id") Long id);

    @GetMapping("/api/restaurants/{id}/exists")
    void validateRestaurant(@PathVariable("id") Long id);
}

