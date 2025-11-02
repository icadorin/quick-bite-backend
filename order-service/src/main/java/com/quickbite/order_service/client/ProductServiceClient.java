package com.quickbite.order_service.client;

import com.quickbite.order_service.dtos.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);

    @GetMapping("/api/products/{id}/exists")
    Boolean validateProduct(@PathVariable Long id);
}

