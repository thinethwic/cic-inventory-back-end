package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.auth.AuthUserResponse;
import com.cic.inventory.dtos.users.UserCreateRequest;
import com.cic.inventory.dtos.users.UserUpdateRequest;
import com.cic.inventory.entities.User;
import com.cic.inventory.entities.types.UserRole;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.UserRepository;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthServiceImpl authService;

    @Override
    public Page<AuthUserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(authService::toUserResponse);
    }

    @Override
    public AuthUserResponse getUserById(Long id) {
        User user = getExistingUser(id);
        return authService.toUserResponse(user);
    }

    @Override
    public AuthUserResponse createUser(UserCreateRequest request, UserPrincipal principal) {
        ensureCanManageRole(principal, UserRole.fromValue(request.role()));

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new InventoryException("A user with this email already exists", HttpStatus.CONFLICT);
        }

        UserRole targetRole = UserRole.fromValue(request.role());
        validateScopeFields(targetRole, request.location(), request.department());

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .location(normalizeScopeValue(request.location()))
                .department(normalizeScopeValue(request.department()))
                .role(targetRole)
                .isActive(request.isActive() == null || request.isActive())
                .build();

        return authService.toUserResponse(userRepository.save(user));
    }

    @Override
    public AuthUserResponse updateUser(Long id, UserUpdateRequest request, UserPrincipal principal) {
        User existingUser = getExistingUser(id);

        UserRole targetRole = request.role() != null ? UserRole.fromValue(request.role()) : existingUser.getRole();
        ensureCanManageRole(principal, targetRole);
        ensureCanTouchTarget(principal, existingUser);

        if (request.email() != null && !request.email().trim().equalsIgnoreCase(existingUser.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new InventoryException("A user with this email already exists", HttpStatus.CONFLICT);
        }

        if (request.firstName() != null && !request.firstName().isBlank()) {
            existingUser.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            existingUser.setLastName(request.lastName().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            existingUser.setEmail(request.email().trim().toLowerCase());
        }
        if (request.location() != null && !request.location().isBlank()) {
            existingUser.setLocation(request.location().trim());
        }
        if (request.department() != null && !request.department().isBlank()) {
            existingUser.setDepartment(request.department().trim());
        } else if (request.department() != null) {
            existingUser.setDepartment("");
        }
        if (request.role() != null && !request.role().isBlank()) {
            existingUser.setRole(targetRole);
        }
        if (request.isActive() != null) {
            existingUser.setActive(request.isActive());
        }
        if (request.password() != null && !request.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.location() != null && request.location().isBlank()) {
            existingUser.setLocation("");
        }

        validateScopeFields(existingUser.getRole(), existingUser.getLocation(), existingUser.getDepartment());

        return authService.toUserResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id, UserPrincipal principal) {
        User user = getExistingUser(id);
        ensureCanTouchTarget(principal, user);
        ensureCanManageRole(principal, user.getRole());
        userRepository.delete(user);
    }

    private User getExistingUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InventoryException("User not found", HttpStatus.NOT_FOUND));
    }

    private void ensureCanManageRole(UserPrincipal principal, UserRole role) {
        UserRole actorRole = UserRole.fromValue(principal.getRole());
        if (actorRole == UserRole.ADMIN) {
            return;
        }
        throw new InventoryException("You do not have permission to manage this role", HttpStatus.FORBIDDEN);
    }

    private void ensureCanTouchTarget(UserPrincipal principal, User target) {
        UserRole actorRole = UserRole.fromValue(principal.getRole());
        if (actorRole == UserRole.ADMIN) {
            return;
        }
        throw new InventoryException("You do not have permission to manage this user", HttpStatus.FORBIDDEN);
    }

    private void validateScopeFields(UserRole role, String location, String department) {
        boolean hasLocation = location != null && !location.isBlank();
        boolean hasDepartment = department != null && !department.isBlank();

        if (role == UserRole.ADMIN) {
            return;
        }

        if (!hasLocation && !hasDepartment) {
            throw new InventoryException(
                    "Location or department is required for non-admin users",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String normalizeScopeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
