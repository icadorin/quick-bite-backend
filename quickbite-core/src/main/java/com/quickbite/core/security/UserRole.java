package com.quickbite.core.security;

public enum UserRole {
    CUSTOMER,
    RESTAURANT_OWNER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
