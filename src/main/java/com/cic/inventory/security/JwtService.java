package com.cic.inventory.security;

import com.cic.inventory.entities.User;
import com.cic.inventory.entities.UserSession;
import com.cic.inventory.repositories.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService implements TokenValidator {

    private final UserSessionRepository userSessionRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-minutes:60}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthTokens generateTokens(User user, UserSession session) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusSeconds(accessTokenExpirationMinutes * 60);
        Instant refreshExpiresAt = now.plusSeconds(refreshTokenExpirationDays * 24 * 60 * 60);
        String accessJti = UUID.randomUUID().toString();

        session.setAccessTokenJti(accessJti);
        session.setExpiresAt(refreshExpiresAt);
        session.setLastUsedAt(now);

        String accessToken = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(accessJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiresAt))
                .claim("type", "access")
                .claim("sid", session.getId().toString())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("location", user.getLocation() != null ? user.getLocation().getName() : null)
                .claim("department", user.getDepartment() != null ? user.getDepartment().getName() : null)
                .claim("role", user.getRole().toClaimValue())
                .claim("roles", List.of(user.getRole().toClaimValue()))
                .signWith(getSigningKey())
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExpiresAt))
                .claim("type", "refresh")
                .claim("sid", session.getId().toString())
                .signWith(getSigningKey())
                .compact();

        return new AuthTokens(accessToken, refreshToken, accessExpiresAt);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                log.debug("JWT validation failed: token type is not access");
                return false;
            }

            UUID sessionId = UUID.fromString(claims.get("sid", String.class));
            Optional<UserSession> sessionOpt = userSessionRepository.findByIdAndRevokedAtIsNull(sessionId);
            if (sessionOpt.isEmpty()) {
                log.debug("JWT validation failed: no active session found for sid={}", sessionId);
                return false;
            }

            UserSession session = sessionOpt.get();
            if (session.getExpiresAt().isBefore(Instant.now())) {
                log.debug("JWT validation failed: session expired for sid={}", sessionId);
                return false;
            }

            User sessionUser = session.getUser();
            if (sessionUser == null) {
                log.debug("JWT validation failed: session user missing for sid={}", sessionId);
                return false;
            }

            if (!sessionUser.isActive()) {
                log.debug("JWT validation failed: inactive user for sid={}, userId={}", sessionId, sessionUser.getId());
                return false;
            }

            return true;
        } catch (Exception exception) {
            log.warn("JWT validation failed: {}: {}", exception.getClass().getSimpleName(), exception.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception exception) {
            log.debug("Refresh token validation failed: {}", exception.getMessage());
            return false;
        }
    }

    public Claims parseClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return claimsJws.getPayload();
    }

    @Override
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public List<String> extractRoles(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    @Override
    public String extractFirstName(String token) {
        return parseClaims(token).get("firstName", String.class);
    }

    @Override
    public String extractLastName(String token) {
        return parseClaims(token).get("lastName", String.class);
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    @Override
    public String extractLocation(String token) {
        return parseClaims(token).get("location", String.class);
    }

    @Override
    public String extractDepartmentName(String token) {
        return parseClaims(token).get("department", String.class);
    }

    public UUID extractSessionId(String token) {
        return UUID.fromString(parseClaims(token).get("sid", String.class));
    }

    public void assertRefreshTokenType(String token) {
        String type = parseClaims(token).get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new com.cic.inventory.exceptions.InventoryException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    public record AuthTokens(String accessToken, String refreshToken, Instant accessTokenExpiresAt) {
    }
}
