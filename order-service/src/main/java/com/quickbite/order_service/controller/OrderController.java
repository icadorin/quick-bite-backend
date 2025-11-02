package com.quickbite.order_service.controller;

import com.quickbite.order_service.dtos.OrderResponse;
import com.quickbite.order_service.dtos.OrderStatusUpdateRequest;
import com.quickbite.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getorder(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(
        @PathVariable Long restaurantId,
        @RequestHeader("X-User-Id") Long userId
    ){
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
        @PathVariable Long id,
        @Valid @RequestBody OrderStatusUpdateRequest request,
        @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") Long userId
    ) throws IllegalAccessException {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId));
    }
}
