package com.quickbite.auth_service.repository.specification;

import com.quickbite.auth_service.dto.filter.UserFilter;
import com.quickbite.auth_service.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withFilters(UserFilter filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("email")),
                        "%%%s%%".formatted(filter.email().toLowerCase())
                    )
                );
            }

            if (filter.role() != null) {
                predicates.add(cb.equal(root.get("role"), filter.role()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
