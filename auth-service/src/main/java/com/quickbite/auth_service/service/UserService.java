package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.UpdateUserRequest;
import com.quickbite.auth_service.dto.UserResponse;
import com.quickbite.auth_service.dto.filter.UserFilter;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.mapper.UserPatchMapper;
import com.quickbite.auth_service.mapper.UserResponseMapper;
import com.quickbite.auth_service.repository.UserRepository;
import com.quickbite.auth_service.repository.specification.UserSpecification;
import com.quickbite.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserResponseMapper responseMapper;
    private final UserPatchMapper patchMapper;

    public UserResponse getById(Long id) {
        User user = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return responseMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        patchMapper.updateUserFromRequest(request, user);

        return responseMapper.toResponse(repository.save(user));
    }

    public Page<UserResponse> findUsers(UserFilter filter, Pageable pageable) {

        return repository.findAll(
            UserSpecification.withFilters(filter),
            pageable
        ).map(responseMapper::toResponse);
    }
}
