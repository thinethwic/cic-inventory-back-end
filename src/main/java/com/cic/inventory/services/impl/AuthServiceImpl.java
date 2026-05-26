package com.cic.inventory.services.impl;

import com.cic.inventory.dtos.auth.*;
import com.cic.inventory.entities.Department;
import com.cic.inventory.entities.Location;
import com.cic.inventory.entities.User;
import com.cic.inventory.entities.UserSession;
import com.cic.inventory.entities.types.UserRole;
import com.cic.inventory.exceptions.InventoryException;
import com.cic.inventory.repositories.DepartmentRepositories;
import com.cic.inventory.repositories.LocationRepositories;
import com.cic.inventory.repositories.UserRepository;
import com.cic.inventory.repositories.UserSessionRepository;
import com.cic.inventory.security.JwtService;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final LocationRepositories locationRepositories;
    private final DepartmentRepositories departmentRepositories;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new InventoryException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new InventoryException("This user account is inactive", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InventoryException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        return createSessionResponse(user);
    }

    @Override
    public AuthResponse register(RegisterRequest request, UserPrincipal principal) {
        if (principal == null || !"admin".equalsIgnoreCase(principal.getRole())) {
            throw new InventoryException("Only admins can register new users", HttpStatus.FORBIDDEN);
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new InventoryException("A user with this email already exists", HttpStatus.CONFLICT);
        }

        UserRole requestedRole = UserRole.fromValue(request.role());

        // ✅ Look up real entities
        Location location = null;
        Department department = null;

        if (requestedRole != UserRole.ADMIN) {
            if (request.locationId() == null && request.departmentId() == null) {
                throw new InventoryException(
                        "Location or department is required for non-admin users",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (request.locationId() != null) {
                location = locationRepositories.findById(request.locationId())
                        .orElseThrow(() -> new InventoryException("Location not found", HttpStatus.NOT_FOUND));
            }

            if (request.departmentId() != null) {
                department = departmentRepositories.findById(request.departmentId())
                        .orElseThrow(() -> new InventoryException("Department not found", HttpStatus.NOT_FOUND));
            }
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .location(location)       // ✅ entity, not string
                .department(department)   // ✅ entity, not string
                .role(requestedRole)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return createSessionResponse(savedUser);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtService.validateRefreshToken(request.refreshToken())) {
            throw new InventoryException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        jwtService.assertRefreshTokenType(request.refreshToken());
        UUID sessionId = jwtService.extractSessionId(request.refreshToken());
        UserSession session = userSessionRepository.findByIdAndRevokedAtIsNull(sessionId)
                .orElseThrow(() -> new InventoryException("Session not found", HttpStatus.UNAUTHORIZED));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevokedAt(Instant.now());
            userSessionRepository.save(session);
            throw new InventoryException("Session has expired", HttpStatus.UNAUTHORIZED);
        }

        String incomingHash = hashToken(request.refreshToken());
        if (!incomingHash.equals(session.getRefreshTokenHash())) {
            session.setRevokedAt(Instant.now());
            userSessionRepository.save(session);
            throw new InventoryException("Refresh token is invalid", HttpStatus.UNAUTHORIZED);
        }

        User user = session.getUser();
        if (user == null || !user.isActive()) {
            throw new InventoryException("User account is inactive", HttpStatus.FORBIDDEN);
        }

        JwtService.AuthTokens tokens = jwtService.generateTokens(user, session);
        session.setRefreshTokenHash(hashToken(tokens.refreshToken()));
        session.setLastUsedAt(Instant.now());
        userSessionRepository.save(session);

        return buildAuthResponse(user, tokens);
    }

    @Override
    public void logout(String accessToken, LogoutRequest request) {
        UUID sessionId = null;

        if (accessToken != null && !accessToken.isBlank()) {
            try {
                sessionId = jwtService.extractSessionId(accessToken);
            } catch (Exception ignored) {
                log.debug("Unable to extract session from access token during logout");
            }
        }

        if (sessionId == null && request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            sessionId = jwtService.extractSessionId(request.refreshToken());
        }

        if (sessionId == null) {
            return;
        }

        userSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            session.setLastUsedAt(Instant.now());
            userSessionRepository.save(session);
        });
    }

    @Override
    public AuthUserResponse getCurrentUser(UserPrincipal principal) {
        Long userId = parseUserId(principal.getId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InventoryException("User not found", HttpStatus.NOT_FOUND));
        return toUserResponse(user);
    }

    public AuthUserResponse toUserResponse(User user) {
        String role = user.getRole().toClaimValue();
        return AuthUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName((user.getFirstName() + " " + user.getLastName()).trim())
                .email(user.getEmail())
                .location(user.getLocation() != null ? user.getLocation().getName() : null)
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .role(role)
                .roles(List.of(role))
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AuthResponse createSessionResponse(User user) {
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash("")
                .accessTokenJti("")
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .build();
        // DO NOT set .id() — let @GeneratedValue assign it

        userSessionRepository.save(session); // ← real UUID assigned by DB here

        JwtService.AuthTokens tokens = jwtService.generateTokens(user, session);
        // session.id is now the real DB UUID, so JWT sid claim matches

        session.setRefreshTokenHash(hashToken(tokens.refreshToken()));
        userSessionRepository.save(session); // ← persist JTI + hash

        return buildAuthResponse(user, tokens);
    }

    private AuthResponse buildAuthResponse(User user, JwtService.AuthTokens tokens) {
        return AuthResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .accessTokenExpiresAt(tokens.accessTokenExpiresAt())
                .user(toUserResponse(user))
                .build();
    }

    private Long parseUserId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new InventoryException("Invalid user id", HttpStatus.BAD_REQUEST);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new InventoryException("Unable to process token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
