package com.quickbite.order_service.repositories.specifications;

import com.quickbite.order_service.dto.filter.OrderFilter;
import com.quickbite.order_service.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> withFilters(OrderFilter filter) {

        return(root, query, cb) -> {

            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.userId() != null) {
                predicates.add(
                    cb.equal(root.get("userId"), filter.userId())
                );
            }

            if (filter.restaurantId() != null) {
                predicates.add(
                    cb.equal(root.get("restaurantId"), filter.restaurantId())
                );
            }

            if (filter.status() != null) {
                predicates.add(
                    cb.equal(root.get("status"), filter.status())
                );
            }

            if (filter.startDate() != null && filter.endDate() != null) {
                predicates.add(
                    cb.between(
                        root.get("createdAt"),
                        filter.startDate(),
                        filter.endDate()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
