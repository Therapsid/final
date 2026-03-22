package com.example.backend.auth.jwt;

import com.example.backend.auth.service.JWTservice;
import com.example.backend.users.service.UsersServices;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTservice jwtservice;
    private final UsersServices userService;
    private final AuthenticationEntryPoint restAuthenticationEntryPoint;
    private final com.example.backend.auth.service.TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                if (tokenBlacklistService.isBlacklisted(token)) {
                    throw new io.jsonwebtoken.JwtException("Token is blacklisted");
                }

                username = jwtservice.extractUsername(token);
            }

            if (username != null && SecurityContextHolder
                    .getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);
                if (jwtservice.validateToken(token, userDetails)) {
                    var claims = jwtservice.extractRoles(token);
                    var authorities = claims.stream()
                            .map(SimpleGrantedAuthority::new).toList();
                    var authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("Unauthorized request. path={}, reason={}", request.getRequestURI(), e.getMessage());
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Invalid or expired JWT: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
        } catch (Exception e) {
            InsufficientAuthenticationException authEx = new InsufficientAuthenticationException("Authentication failed: " + e.getMessage());
            restAuthenticationEntryPoint.commence(request, response, authEx);
        }
    }
}
