package com.quickbite.order_service.client;

import com.quickbite.order_service.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/v1/restaurants/{id}/exists")
    boolean validateRestaurant(@PathVariable("id") Long id);
}
