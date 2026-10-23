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
                                "/login", "/login/**", "/register", "/verification-sent", "/verify-email", "/forgot-password", "/reset-password",
                                "/css/**", "/js/**", "/images/**", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/account/**").hasRole("CUSTOMER")
                        .requestMatchers("/staff/**").hasRole("AGENT")
                        .requestMatchers("/operations/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            attempts.succeeded(LoginRateLimitFilter.key(request));
                            response.sendRedirect(homeFor(authentication));
                        })
                        .failureHandler((request, response, exception) -> {
                            attempts.failed(LoginRateLimitFilter.key(request));
                            String portal = request.getParameter("portal");
                            String target = switch (portal == null ? "" : portal) {
                                case "customer", "staff", "admin" -> "/login/" + portal + "?error";
                                default -> "/login?error";
                            };
                            response.sendRedirect(target);
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

    private static String homeFor(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) return "/admin";
        if (hasRole(authentication, "ROLE_AGENT")) return "/staff";
        return "/account";
    }
}
