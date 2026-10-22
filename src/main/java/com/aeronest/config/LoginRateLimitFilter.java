package com.aeronest.config;

import com.aeronest.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final LoginAttemptService attempts;
    public LoginRateLimitFilter(LoginAttemptService attempts) { this.attempts = attempts; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if ("POST".equals(request.getMethod()) && "/login".equals(request.getServletPath()) && attempts.isBlocked(key(request))) {
            response.sendRedirect("/login?locked"); return;
        }
        chain.doFilter(request, response);
    }
    public static String key(HttpServletRequest request) { return request.getRemoteAddr() + ":" + String.valueOf(request.getParameter("username")).toLowerCase(); }
}
