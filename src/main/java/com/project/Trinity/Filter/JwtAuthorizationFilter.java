
package com.project.Trinity.Filter;

import com.project.Trinity.Service.UserService;
import com.project.Trinity.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }
    

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // /api/auth/register, /api/auth/login, /api/auth/refresh-token, /api/auth/forgot-password, /api/auth/reset-password için JWT doğrulama atla
        if (requestURI.startsWith("/api/auth/register") ||
            requestURI.startsWith("/api/auth/login") ||
            requestURI.startsWith("/api/auth/refresh-token") ||
            requestURI.startsWith("/api/auth/forgot-password") ||
            requestURI.startsWith("/api/auth/reset-password")) {
            logger.debug("Skipping JWT validation for endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Bearer token found for request: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtUtil.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authenticated user for request: {}", requestURI);
            } else {
                logger.warn("Invalid JWT token for user: {}", username);
            }
        }
        filterChain.doFilter(request, response);
    }}