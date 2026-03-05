package com.quickbite.order_service.domain;

import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.dto.OrderItemRequest;
import com.quickbite.order_service.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class OrderCalculator {

    public BigDecimal calculateTotal(
        List<OrderItemRequest> items,
        Map<Long, ProductResponse> products
    ) {
        return items.stream()
            .map(item -> {

                ProductResponse product = products.get(item.getProductId());

                if (product == null || !Boolean.TRUE.equals(product.getIsAvailable())) {
                    throw new BusinessRuleViolationException(
                        "Product not available: " + item.getProductId()
                    );
                }

                return product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
