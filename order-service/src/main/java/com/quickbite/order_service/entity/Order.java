package com.quickbite.order_service.entity;

import com.quickbite.core.entity.BaseEntity;
import com.quickbite.core.exception.BusinessRuleViolationException;
import com.quickbite.order_service.model.DeliveryAddress;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true,  callSuper = false)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "delivery_address", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private DeliveryAddress deliveryAddress;

    @Column(name = "customer_notes", length = 500)
    private String customerNotes;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP,
        OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }

    public void changeStatus(OrderStatus newStatus, String notes) {
        if (this.status == newStatus) {
            throw new BusinessRuleViolationException(
                "Order already in this status"
            );
        }

        if (!isValidTransition(this.status, newStatus)) {
            throw new BusinessRuleViolationException(
                "Invalid status transition: " + this.status + " → " + newStatus
            );
        }

        this.status = newStatus;

        if (newStatus == OrderStatus.DELIVERED) {
            this.actualDeliveryTime = LocalDateTime.now();
        }

        OrderStatusHistory history =
            OrderStatusHistory.builder()
                .order(this)
                .status(newStatus)
                .notes(notes)
                .build();

        this.statusHistory.add(history);
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(item -> item.getTotalPrice() != null
                ? item.getTotalPrice()
                : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP),
        OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.OUT_FOR_DELIVERY),
        OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED, Set.of(),
        OrderStatus.CANCELLED, Set.of()
    );

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return VALID_TRANSITIONS
            .getOrDefault(current, Set.of())
            .contains(next);
    }
}
