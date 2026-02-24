package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.RestaurantFilter;
import com.quickbite.product_service.entity.Restaurant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RestaurantSpecification {

    private RestaurantSpecification() {}

    public static Specification<Restaurant> withFilters(RestaurantFilter filter) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.isActive() != null) {
                predicates.add(
                    filter.isActive()
                        ? cb.isTrue(root.get("isActive"))
                        : cb.isFalse(root.get("isActive"))
                );
            }

            if (StringUtils.hasText(filter.name())) {
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
                    cb.equal(root.get("owner").get("id"), filter.ownerId())
                );
            }

            if (filter.cuisineType() != null) {
                predicates.add(
                    cb.equal(root.get("cuisineType"), filter.cuisineType())
                );
            }

            if (filter.minRating() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("rating"),
                        filter.minRating()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Restaurant> onlyActive() {
        return (root, query, cb) ->
            cb.isTrue(root.get("isActive"));
    }
}
