package com.quickbite.order_service.controller;

import com.quickbite.order_service.constants.ApiPaths;
import com.quickbite.order_service.dto.*;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.security.JwtUser;
import com.quickbite.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping(ApiPaths.ORDER_ID)
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

    @GetMapping(ApiPaths.BY_RESTAURANT)
    public Page<OrderResponse> getRestaurantOrders(
        @PathVariable("restaurantId") @Positive Long restaurantId,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ){
        return orderService.getRestaurantOrders(restaurantId, pageable);
    }

    @GetMapping("/search")
    public Page<OrderResponse> searchOrders(
        OrderFilter filter,
        Pageable pageable
    ) {
        return orderService.searchOrders(filter, pageable);
    }

    @GetMapping(ApiPaths.BY_RESTAURANT + "/stats")
    @PreAuthorize("hasRole('RESTAURANT')")
    public OrderStatusResponse getRestaurantStats(
        @PathVariable("restaurantId") @Positive Long restaurantId
    ) {
        return orderService.getRestaurantStats(restaurantId);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody OrderRequest request,
        @AuthenticationPrincipal JwtUser user
    ) {
        OrderResponse response = orderService.createOrder(request, user.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(ApiPaths.ORDER_ID + ApiPaths.STATUS)
    @PreAuthorize("hasRole('RESTAURANT')")
    public OrderResponse updateOrderStatus(
        @PathVariable("id") @Positive Long id,
        @Valid @RequestBody OrderStatusUpdateRequest request,
        @AuthenticationPrincipal JwtUser user
    ) {
        return orderService.updateOrderStatus(
            id,
            request,
            user.id(),
            user.restaurantId(),
            user.role()
        );
    }

    @PostMapping(ApiPaths.ORDER_ID + ApiPaths.CANCEL)
    public OrderResponse cancelOrder(
        @PathVariable("id") @Positive Long id,
        @AuthenticationPrincipal JwtUser user
    ) {
        return orderService.cancelOrder(
            id,
            user.id(),
            user.restaurantId(),
            user.role()
        );
    }
}
