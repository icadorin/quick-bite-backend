package com.quickbite.product_service.repository.specification;

import com.quickbite.product_service.dto.filter.CategoryFilter;
import com.quickbite.product_service.entity.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CategorySpecification {

    private CategorySpecification() {}

    public static Specification<Category> withFilters(CategoryFilter filter) {

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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Category> onlyActive() {
        return (root, query, cb) ->
            cb.isTrue(root.get("isActive"));
    }
}
