package com.cic.inventory.services;

import com.cic.inventory.dtos.auth.*;
import com.cic.inventory.security.UserPrincipal;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request, UserPrincipal principal);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String accessToken, LogoutRequest request);
    AuthUserResponse getCurrentUser(UserPrincipal principal);
    AuthUserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request);
    void changePassword(UserPrincipal principal, ChangePasswordRequest request);
}
