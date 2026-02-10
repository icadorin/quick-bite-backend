package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.ProductFilter;
import com.quickbite.product_service.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilters(ProductFilter filter) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            predicate.getExpressions().add(
                cb.isTrue(root.get("isAvailable"))
            );

            predicate.getExpressions().add(
                cb.isTrue(root.get("restaurant").get("isActive"))
            );

            if (filter.restaurantId() != null) {
                predicate.getExpressions().add(
                    cb.equal(root.get("restaurant").get("id"), filter.restaurantId())
                );
            }

            if (filter.categoryId() != null) {
                predicate.getExpressions().add(
                    cb.equal(root.get("category").get("id"), filter.categoryId())
                );
            }

            if (filter.name() != null && filter.name().length() >= 3) {
                predicate.getExpressions().add(
                    cb.like(
                        cb.lower(root.get("name")),
                        "%%%s%%".formatted(filter.name().toLowerCase())
                    )
                );
            }

            if (filter.minPrice() != null) {
                predicate.getExpressions().add(
                    cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice())
                );
            }

            if (filter.maxPrice() != null) {
                predicate.getExpressions().add(
                    cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice())
                );
            }

            return predicate;
        };
    }
}
