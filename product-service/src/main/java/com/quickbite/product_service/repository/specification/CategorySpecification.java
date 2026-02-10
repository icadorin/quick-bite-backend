package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public final class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> withFilters(CategoryFilter filter) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            if (filter.isActive() == null || filter.isActive()) {
                predicate.getExpressions().add(
                    cb.isTrue(root.get("isActive"))
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

            return predicate;
        };
    }
}
