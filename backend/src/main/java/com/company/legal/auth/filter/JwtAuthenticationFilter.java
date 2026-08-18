package com.company.legal.auth.filter;

import com.company.legal.auth.service.JwtService;
import com.company.legal.user.entity.User;
import com.company.legal.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT authentication filter for stateless API authentication.
 *
 * This filter:
 * - Extracts JWT tokens from the Authorization header
 * - Validates the token signature and expiration
 * - Loads user details from the database
 * - Sets the authentication in the SecurityContext
 *
 * The filter runs before the UsernamePasswordAuthenticationFilter
 * and is executed on every request to authenticated endpoints.
 *
 * @author LPMS Security Team
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHENTICATION_ATTR = "AUTHENTICATION";

    /**
     * Processes the incoming request to validate JWT authentication.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT from request
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && !jwt.isEmpty()) {
                log.debug("JWT token found in request");

                // Validate token format and signature
                if (!jwtService.isTokenValid(jwt)) {
                    log.warn("Invalid JWT token detected");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract username (email) from token
                String email = jwtService.extractEmail(jwt);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Extracted email from JWT: {}", email);

                    // Load user from database
                    Optional<User> userOptional = userRepository.findByEmail(email);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        // Check if user is active
                        if (user.getStatus() == User.UserStatus.ACTIVE) {
                            // Check if user account is not locked
                            if (user.isAccountNonLocked()) {
                                // Validate token against user details
                                if (jwtService.isTokenValidForUser(jwt, user)) {
                                    log.debug("JWT token validated successfully for user: {}", email);

                                    // Create authentication token
                                    UsernamePasswordAuthenticationToken authentication =
                                            createAuthenticationToken(user, request);

                                    // Set in security context
                                    SecurityContextHolder.getContext().setAuthentication(authentication);

                                    log.info("User authenticated successfully: {}", email);
                                } else {
                                    log.warn("Token validation failed for user: {}", email);
                                }
                            } else {
                                log.warn("User account is locked: {}", email);
                            }
                        } else {
                            log.warn("User account is not active: {}, status: {}", email, user.getStatus());
                        }
                    } else {
                        log.warn("User not found: {}", email);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error processing JWT authentication: {}", ex.getMessage(), ex);
            // Don't fail the request, let it proceed and be handled by exception handler
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token without the "Bearer " prefix, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());

            // Basic validation - ensure token is not empty after extraction
            if (StringUtils.hasText(token)) {
                log.debug("JWT token extracted from header");
                return token;
            }
        }

        log.debug("No JWT token found in Authorization header");
        return null;
    }

    /**
     * Creates an authentication token for the authenticated user.
     *
     * @param user the authenticated user
     * @param request the HTTP request
     * @return the authentication token
     */
    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            User user,
            HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities());

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        return authentication;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT filtering for public endpoints
        String path = request.getRequestURI();

        return path.startsWith("/v1/auth/") ||
               path.equals("/v1/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/") ||
               path.equals("/error");
    }
}
