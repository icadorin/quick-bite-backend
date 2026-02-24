package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilters(ProductFilter filter) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.restaurantId() != null) {
                predicates.add(
                    cb.equal(
                        root.get("restaurant").get("id"),
                        filter.restaurantId()
                    )
                );
            }

            if (filter.categoryId() != null) {
                predicates.add(
                    cb.equal(
                        root.get("category").get("id"),
                        filter.categoryId()
                    )
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

            if (Boolean.TRUE.equals(filter.onlyAvailable())) {
                predicates.add(cb.isTrue(root.get("isAvailable")));
                predicates.add(cb.isTrue(root.get("restaurant").get("isActive")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> onlyAvailable() {
        return (root, query, cb) -> cb.and(
            cb.isTrue(root.get("isAvailable")),
            cb.isTrue(root.get("restaurant").get("isActive"))
        );
    }

    public static Specification<Product> featured() {
        return (root, query, cb) ->
            cb.isTrue(root.get("isFeatured"));
    }
}
