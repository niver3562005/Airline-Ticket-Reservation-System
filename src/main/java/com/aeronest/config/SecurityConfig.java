package com.aeronest.config;

import com.aeronest.service.LoginAttemptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    SecurityFilterChain security(HttpSecurity http, LoginAttemptService attempts) throws Exception {
        LoginRateLimitFilter rateLimitFilter = new LoginRateLimitFilter(attempts);
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/flights", "/flight-schedule", "/book/**", "/booking/**", "/manage/**", "/check-in/**",
                                "/travel-hub", "/flight-status", "/airport-guide/**", "/packages", "/support",
                                "/travel-services/**", "/cabin-guide",
                                "/deals", "/payment-options",
                                "/privacy-policy", "/cookie-policy", "/legal-notice",
                                "/login", "/register", "/verification-sent", "/verify-email", "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/account/**").hasRole("CUSTOMER")
                        .requestMatchers("/admin/airports/**", "/admin/aircraft/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "AGENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            attempts.succeeded(LoginRateLimitFilter.key(request));
                            response.sendRedirect(hasRole(authentication, "ROLE_CUSTOMER") ? "/account" : "/admin");
                        })
                        .failureHandler((request, response, exception) -> {
                            attempts.failed(LoginRateLimitFilter.key(request)); response.sendRedirect("/login?error");
                        }).permitAll())
                .rememberMe(remember -> remember.key("aerovista-local-development-key")
                        .tokenValiditySeconds(14 * 24 * 60 * 60).rememberMeParameter("remember-me"))
                .logout(logout -> logout.logoutSuccessUrl("/?logout").invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me"))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()).maximumSessions(3))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}
