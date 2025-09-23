package com.quickbite.auth_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public enum UserRole {
        CUSTOMER, RESTAURANT_OWNER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }
}
