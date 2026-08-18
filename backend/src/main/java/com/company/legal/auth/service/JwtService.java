package com.company.legal.auth.service;

import com.company.legal.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Service for token generation, validation, and extraction.
 *
 * Provides comprehensive JWT functionality including:
 * - Access token generation with configurable expiration
 * - Refresh token generation with longer expiration
 * - Token validation (signature, expiration, claims)
 * - Claims extraction from tokens
 * - Email extraction from token subject
 * - Token refresh detection
 *
 * Uses HS256 algorithm with configurable secret key.
 * All secrets are loaded from environment variables.
 *
 * @author LPMS Security Team
 * @version 2.0.0
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${app.jwt.issuer:LPMS-Backend}")
    private String jwtIssuer;

    private volatile SecretKey cachedKey;

    /**
     * Gets the signing key, caching it for performance.
     *
     * The key is derived from the JWT secret configured in environment variables.
     * The secret must be at least 256 bits (32 characters) for HS256.
     *
     * @return the SecretKey for signing and verifying tokens
     */
    private SecretKey getSigningKey() {
        if (cachedKey == null) {
            synchronized (this) {
                if (cachedKey == null) {
                    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

                    // Ensure key is at least 256 bits for HS256
                    if (keyBytes.length < 32) {
                        throw new IllegalStateException(
                                "JWT secret must be at least 256 bits (32 characters)");
                    }

                    cachedKey = Keys.hmacShaKeyFor(keyBytes);
                    log.info("JWT signing key initialized");
                }
            }
        }
        return cachedKey;
    }

    /**
     * Generates an access token for the given user details.
     *
     * @param userDetails the user details
     * @return the generated JWT access token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates an access token with extra claims.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @return the generated JWT access token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Extract user roles and add to claims
        Map<String, Object> claims = new HashMap<>(extraClaims);

        if (userDetails instanceof User user) {
            claims.put("roles", user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
            claims.put("userId", user.getId());
            claims.put("lawFirmId", user.getLawFirmId());

            if (user.getBranchId() != null) {
                claims.put("branchId", user.getBranchId());
            }
        }

        return buildToken(claims, userDetails, jwtExpirationMs);
    }

    /**
     * Generates a refresh token with extended expiration.
     *
     * Refresh tokens have longer expiration and are used
     * to obtain new access tokens without re-authentication.
     *
     * @param userDetails the user details
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        if (userDetails instanceof User user) {
            claims.put("userId", user.getId());
        }

        return buildToken(claims, userDetails, refreshExpirationMs);
    }

    /**
     * Builds a JWT token with the specified claims and expiration.
     *
     * @param claims the claims to include in the token
     * @param userDetails the user details for subject
     * @param expirationMs the expiration time in milliseconds
     * @return the constructed JWT token
     */
    private String buildToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long expirationMs) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiration)
                .notBefore(now)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extracts the email (username) from a JWT token.
     *
     * @param token the JWT token
     * @return the email address from the token subject
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param <T> the type of the claim
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims body from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if a token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validates a JWT token for the given user details.
     *
     * This method checks:
     * - Token signature validity
     * - Token expiration
     * - Token subject matches user
     *
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid for the user
     */
    public boolean isTokenValidForUser(String token, UserDetails userDetails) {
        final String email = extractEmail(token);

        // Check if token is expired
        if (isTokenExpired(token)) {
            log.debug("Token has expired");
            return false;
        }

        // Check if token subject matches user
        boolean usernameMatches = email.equals(userDetails.getUsername());

        // Check if user account is still valid
        boolean userAccountValid = userDetails.isEnabled() &&
                userDetails.isAccountNonLocked() &&
                userDetails.isCredentialsNonExpired();

        boolean isValid = usernameMatches && userAccountValid;

        if (!isValid) {
            log.debug("Token validation failed: usernameMatches={}, userAccountValid={}",
                    usernameMatches, userAccountValid);
        }

        return isValid;
    }

    /**
     * Validates a JWT token.
     *
     * This method performs comprehensive validation including:
     * - Signature verification
     * - Expiration check
     * - Token format validation
     *
     * @param token the JWT token to validate
     * @return true if the token is valid
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            log.debug("Token is null or empty");
            return false;
        }

        try {
            // Parse and verify the token
            Claims claims = extractAllClaims(token);

            // Check expiration
            if (isTokenExpired(token)) {
                log.debug("Token has expired");
                return false;
            }

            // Check if token is a refresh token (access tokens should not be refresh tokens)
            String tokenType = claims.get("type", String.class);
            if ("refresh".equals(tokenType)) {
                log.debug("Invalid token type: refresh token used as access token");
                return false;
            }

            log.debug("Token validation successful");
            return true;

        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims are empty or invalid: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if a token has been refreshed (newer than the provided timestamp).
     *
     * @param token the JWT token
     * @param timestamp the timestamp to compare against
     * @return true if the token was issued after the timestamp
     */
    public boolean isTokenRefreshed(String token, Date timestamp) {
        final Date issuedAt = extractClaim(token, Claims::getIssuedAt);
        return issuedAt.after(timestamp);
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID, or null if not present
     */
    public String extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object userId = claims.get("userId");
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the law firm ID from a JWT token.
     *
     * @param token the JWT token
     * @return the law firm ID, or null if not present
     */
    public String extractLawFirmId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object lawFirmId = claims.get("lawFirmId");
            return lawFirmId != null ? lawFirmId.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract law firm ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the roles from a JWT token.
     *
     * @param token the JWT token
     * @return list of role names, empty list if not present
     */
    public java.util.List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object roles = claims.get("roles");

            if (roles instanceof java.util.List) {
                return ((java.util.List<?>) roles).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }

            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to extract roles from token: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Gets the token expiration time in milliseconds.
     *
     * @return the access token expiration time
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Gets the refresh token expiration time in milliseconds.
     *
     * @return the refresh token expiration time
     */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
