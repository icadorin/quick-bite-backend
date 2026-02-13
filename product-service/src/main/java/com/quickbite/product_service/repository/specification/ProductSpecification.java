package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Product;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilters(ProductFilter filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isAvailable")));
            predicates.add(cb.isTrue(root.get("restaurant").get("isActive")));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.restaurantId() != null) {
                var restaurantJoin = root.join("restaurant");

                predicates.add(
                    cb.equal(
                        restaurantJoin.get("id"),
                        filter.restaurantId()
                    )
                );
            }

            if (filter.categoryId() != null) {
                var categoryJoin = root.join("category", JoinType.LEFT);

                predicates.add(
                    cb.equal(
                        categoryJoin.get("id"),
                        filter.categoryId()
                    )
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

            if (filter.minPrice() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("price"),
                        filter.minPrice()
                    )
                );
            }

            if (filter.maxPrice() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("price"),
                        filter.maxPrice()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
