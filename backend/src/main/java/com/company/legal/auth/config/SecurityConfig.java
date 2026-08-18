package com.company.legal.auth.config;

import com.company.legal.auth.filter.JwtAuthenticationFilter;
import com.company.legal.auth.security.JwtAuthenticationEntryPoint;
import com.company.legal.common.filter.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for LPMS backend.
 *
 * Provides comprehensive security features including:
 * - JWT authentication filter for stateless API security
 * - BCrypt password encoding with configurable strength
 * - CORS configuration for frontend integration
 * - Redis-based rate limiting to prevent abuse
 * - CSRF disable for stateless API (token-based auth)
 * - Method-level security with @PreAuthorize annotations
 *
 * @author LPMS Security Team
 * @version 2.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final UserDetailsService userDetailsService;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${app.cors.max-age}")
    private Long maxAge;

    @Value("${app.security.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Main security filter chain configuration.
     *
     * Configures:
     * - CSRF disable for stateless JWT authentication
     * - CORS with configurable origins
     * - Exception handling with custom authentication entry point
     * - Stateless session management
     * - Request authorization rules
     * - JWT authentication filter
     * - Rate limiting filter
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http
            // CSRF Configuration - Disabled for stateless JWT API
            // CSRF protection is not needed for stateless APIs using JWT tokens
            // The JWT token itself provides CSRF protection
            .csrf(AbstractHttpConfigurer::disable)

            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Exception Handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .accessDeniedHandler(new com.company.legal.common.filter.AuthenticatedAccessDeniedHandler()))

            // Session Management - Stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/verify-email").permitAll()

                // Health and monitoring endpoints
                .requestMatchers("/v1/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()

                // Swagger/OpenAPI endpoints
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs.yaml").permitAll()

                // Public error endpoint
                .requestMatchers("/error").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated())

            // Authentication Provider
            .authenticationProvider(authenticationProvider())

            // Add filters in order
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    /**
     * CORS configuration source bean.
     *
     * Configures Cross-Origin Resource Sharing with:
     * - Allowed origins from configuration
     * - Configurable HTTP methods
     * - Allowed headers
     * - Exposed response headers
     * - Credentials support for cookie-based auth
     * - Preflight cache duration
     *
     * @return the CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Configuring CORS with origins: {}", allowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Authentication provider for username/password authentication.
     *
     * Configures DaoAuthenticationProvider with:
     * - Custom UserDetailsService for loading user details
     * - BCryptPasswordEncoder for password validation
     *
     * @return the AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Configuring authentication provider");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        // Hide user not found exceptions for security
        authProvider.setHideUserNotFoundExceptions(true);

        return authProvider;
    }

    /**
     * AuthenticationManager bean for programmatic authentication.
     *
     * @param config the AuthenticationConfiguration
     * @return the AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder bean using BCrypt with strength 12.
     *
     * BCrypt strength of 12 provides:
     * - Strong resistance to brute-force attacks
     * - Reasonable performance for production use
     * - Industry standard for password hashing
     *
     * @return the PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Creating BCryptPasswordEncoder with strength 12");
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CSRF token repository for endpoints that require CSRF protection.
     *
     * Currently disabled for stateless API, but available if needed
     * for specific endpoints.
     *
     * @return the CsrfTokenRepository
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }
}