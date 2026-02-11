package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.entity.Restaurant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RestaurantSpecification {

    private RestaurantSpecification() {}

    public static Specification<Restaurant> withFilters(RestaurantFilter filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.isActive() != null) {
                predicates.add(
                    filter.isActive()
                    ? cb.isTrue(root.get("isActive"))
                    : cb.isFalse(root.get("isActive"))
                );
            }

            if (filter.name() != null && !filter.name().isBlank()) {
                String pattern = "%%%s%%".formatted(filter.name().toLowerCase(Locale.ROOT));

                predicates.add(
                    cb.like(
                        cb.lower(root.get("name")),
                        pattern
                    )
                );
            }

            if (filter.ownerId() != null) {
                predicates.add(
                    cb.equal(
                        root.get("ownerId"),
                        filter.ownerId()
                    )
                );
            }

            if (filter.cuisineType() != null && !filter.cuisineType().isBlank()) {
                predicates.add(
                    cb.equal(
                        root.get("cuisineType"),
                        filter.cuisineType()
                    )
                );
            }

            if (filter.minRating() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("averageRating"),
                        filter.minRating()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
