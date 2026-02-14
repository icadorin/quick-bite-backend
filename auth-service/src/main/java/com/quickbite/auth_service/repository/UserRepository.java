package com.quickbite.auth_service.repository;

import com.quickbite.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends
    JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT COUNT(u) > 0
        FROM User u
        WHERE LOWER(u.email) = LOWER(:email)
    """)
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}
