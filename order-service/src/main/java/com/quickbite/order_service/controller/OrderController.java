package com.quickbite.order_service.controller;

import com.quickbite.order_service.constants.ApiPaths;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.OrderStatusUpdateRequest;
import com.quickbite.order_service.security.JwtUser;
import com.quickbite.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.ORDERS)
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getUserOrders(
        @AuthenticationPrincipal JwtUser user
        ) {
        return orderService.getUserOrders(user.id());
    }

    @GetMapping(ApiPaths.BY_ID)
    public OrderResponse getOrderById(
        @PathVariable("id") @Positive Long id,
        @AuthenticationPrincipal JwtUser user
        ) {
        return orderService.getOrderById(
            id,
            user.id(),
            user.role()
        );
    }

    @GetMapping(ApiPaths.RESTAURANT + ApiPaths.BY_RESTAURANT_ID)
    public List<OrderResponse> getRestaurantOrders(
        @PathVariable("restaurantId") @Positive Long restaurantId
    ){
        return orderService.getRestaurantOrders(restaurantId);
    }

    @PostMapping
    public OrderResponse createOrder(
        @Valid @RequestBody OrderRequest request,
        @AuthenticationPrincipal JwtUser user
    ) {
        return orderService.createOrder(request, user.id());
    }

    @PostMapping(ApiPaths.BY_ID + ApiPaths.STATUS)
    public OrderResponse updateOrderStatus(
        @PathVariable("id") @Positive Long id,
        @Valid @RequestBody OrderStatusUpdateRequest request,
        @AuthenticationPrincipal JwtUser user
    ) {
        return orderService.updateOrderStatus(id, request, user.id(), user.role());
    }

    @PostMapping(ApiPaths.BY_ID + ApiPaths.CANCEL)
    public OrderResponse cancelOrder(
        @PathVariable("id") @Positive Long id,
        @AuthenticationPrincipal JwtUser user
    ) {
        return orderService.cancelOrder(id, user.id(), user.role());
    }
}
