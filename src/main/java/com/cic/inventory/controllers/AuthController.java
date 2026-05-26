package com.cic.inventory.controllers;

import com.cic.inventory.dtos.auth.*;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends AbstractController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return sendOkResponse(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication
    ) {
        UserPrincipal principal = authentication != null ? (UserPrincipal) authentication.getPrincipal() : null;
        return sendCreatedResponse(authService.register(request, principal));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return sendOkResponse(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String header = httpServletRequest.getHeader("Authorization");
        String accessToken = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        authService.logout(accessToken, request);
        return sendNoContentResponse();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return sendOkResponse(authService.getCurrentUser(principal));
    }
}
