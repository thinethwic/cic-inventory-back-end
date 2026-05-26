package com.cic.inventory.services;

import com.cic.inventory.dtos.auth.AuthUserResponse;
import com.cic.inventory.dtos.users.UserCreateRequest;
import com.cic.inventory.dtos.users.UserUpdateRequest;
import com.cic.inventory.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {
    Page<AuthUserResponse> getUsers(Pageable pageable);
    AuthUserResponse getUserById(Long id);
    AuthUserResponse createUser(UserCreateRequest request, UserPrincipal principal);
    AuthUserResponse updateUser(Long id, UserUpdateRequest request, UserPrincipal principal);
    void deleteUser(Long id, UserPrincipal principal);
}
