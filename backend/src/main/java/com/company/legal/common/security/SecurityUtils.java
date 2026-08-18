package com.company.legal.common.security;

import com.company.legal.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for extracting the authenticated principal's identity from the
 * {@link SecurityContextHolder}.
 *
 * <p>The security context principal is the application's {@link User} entity
 * (loaded by {@code CustomUserDetailsService} and set as the principal by both
 * the {@code DaoAuthenticationProvider} during login and the
 * {@code JwtAuthenticationFilter} on subsequent requests).
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * @return the authenticated user, or {@code null} if there is no authentication.
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return (principal instanceof User user) ? user : null;
    }

    /**
     * @return the authenticated user's id, or {@code null} if unauthenticated.
     */
    public static String getUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * @return the authenticated user's email (used as the JWT subject), or {@code null}.
     */
    public static String getUserEmail() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * @return the authenticated user's law firm id, or {@code null} if unauthenticated.
     */
    public static String getLawFirmId() {
        User user = getCurrentUser();
        return user != null ? user.getLawFirmId() : null;
    }

    /**
     * @return the authenticated user's id (used as the acting lawyer/staff id), or {@code null}.
     */
    public static String getCurrentLawyerId() {
        return getUserId();
    }
}
