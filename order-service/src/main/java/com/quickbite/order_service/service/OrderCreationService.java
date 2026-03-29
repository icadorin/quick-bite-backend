package com.quickbite.order_service.service;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.client.ProductServiceClient;
import com.quickbite.order_service.dto.OrderRequest;
import com.quickbite.order_service.dto.OrderResponse;
import com.quickbite.order_service.dto.ProductResponse;
import com.quickbite.order_service.entity.Order;
import com.quickbite.order_service.entity.OrderItem;
import com.quickbite.order_service.mappers.OrderCreateMapper;
import com.quickbite.order_service.mappers.OrderItemCreateMapper;
import com.quickbite.order_service.mappers.OrderResponseMapper;
import com.quickbite.order_service.repositories.OrderRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper responseMapper;
    private final OrderItemCreateMapper itemMapper;
    private final OrderCreateMapper createMapper;
    private final ProductServiceClient productClient;

    @Transactional
    public OrderResponse createOrder(
        OrderRequest request,
        Long userId
    ) {

        boolean restaurantExists;

        try {
             restaurantExists =
                productClient.validateRestaurant(request.getRestaurantId());
        } catch (FeignException ex) {
            throw new BusinessRuleViolationException(
                "Error validating restaurant"
            );
        }

        if (!restaurantExists) {
            throw new BusinessRuleViolationException(
                "Restaurant not found: " + request.getRestaurantId()
            );
        }

        Order order = createMapper.toEntity(request);

        order.setUserId(userId);

        request.getItems().forEach(itemRequest -> {

            ProductResponse product;

            try {
                product = productClient.getProduct(itemRequest.getProductId());
            } catch (FeignException.NotFound ex) {
                throw new BusinessRuleViolationException(
                    "Product not found: " + itemRequest.getProductId()
                );
            } catch (FeignException ex) {
                throw new BusinessRuleViolationException(
                    "Product service error: " + ex.status()
                );
            }

            validateProductAvailable(product, itemRequest.getProductId());

            OrderItem item = itemMapper.toEntity(itemRequest);

            item.defineProductData(product.getName(), product.getPrice());

            item.calculateTotalPrice();

            order.addItem(item);
        });

        order.recalculateTotal();

        Order saved = orderRepository.save(order);

        return responseMapper.toResponse(saved);
    }

    private void validateProductAvailable(ProductResponse product, Long productId) {
        if (!Boolean.TRUE.equals(product.getIsAvailable())) {
            throw new BusinessRuleViolationException(
                "Product " + productId + " is unavailable"
            );
        }
    }
}
