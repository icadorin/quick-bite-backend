package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> withFilters(CategoryFilter filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                predicates.add(cb.isTrue(root.get("isActive")));
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.isActive() == null || filter.isActive()) {
                predicates.add(cb.isTrue(root.get("isActive")));
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
