package com.quickbite.auth_service.controller;

import com.quickbite.auth_service.constants.ApiPaths;
import com.quickbite.auth_service.dto.UpdateUserRequest;
import com.quickbite.auth_service.dto.UserResponse;
import com.quickbite.auth_service.dto.filter.UserFilter;
import com.quickbite.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.canManageUser(#id)")
    public UserResponse getUser(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.canManageUser(#id)")
    public UserResponse updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return service.updateUser(id, request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> listUsers(
        UserFilter filter,
        Pageable pageable
    ) {
        return service.findUsers(filter, pageable);
    }
}
