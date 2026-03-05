package com.quickbite.order_service.service;

import com.quickbite.order_service.client.aggregator.ExternalOrderDataLoader;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.RestaurantResponse;
import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import com.quickbite.order_service.repositories.specifications.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;
    private final ExternalOrderDataLoader externalLoader;

    public List<OrderResponse> getUserOrders(Long userId) {

        var spec = OrderSpecification.withFilters(
            new OrderFilter(userId, null, null, null, null)
        );

        List<Order> orders = orderRepository.findAll(spec);

        List<Long> restaurantIds = orders.stream()
            .map(Order::getRestaurantId)
            .distinct()
            .toList();

        var restaurants =
            externalLoader.loadRestaurants(restaurantIds);

        return orders.stream()
            .map(order -> enrichOrders(order, restaurants))
            .toList();
    }

    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {

        var spec = OrderSpecification.withFilters(
            new OrderFilter(null, restaurantId, null, null, null)
        );

        List<Order> orders = orderRepository.findAll(spec);

        List<Long> restaurantIds = orders.stream()
            .map(Order::getRestaurantId)
            .distinct()
            .toList();

        var restaurants =
            externalLoader.loadRestaurants(restaurantIds);

        return orders.stream()
            .map(order -> enrichOrders(order, restaurants))
            .toList();
    }

    public OrderResponse getOrderById(Order order) {

        Map<Long, RestaurantResponse> restaurant =
            externalLoader.loadRestaurants(
                List.of(order.getRestaurantId())
            );

        return enrichOrders(order, restaurant);
    }

    private OrderResponse enrichOrders(
        Order order,
        Map<Long, RestaurantResponse> restaurants
    ) {
        OrderResponse response =
             responseMapper.toResponse(order);

        RestaurantResponse restaurant =
            restaurants.get(order.getRestaurantId());

        if (restaurant != null) {
            response.setRestaurantName(restaurant.getName());
        }

        return response;
    }
}
