package com.example.backend.auth.service.impl;

import com.example.backend.auth.service.JWTservice;
import com.example.backend.users.entity.Role;
import com.example.backend.users.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
@Primary
public class JWTserviceImpl implements JWTservice {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getKey() {
        return key;
    }

    private static final long ACCESS_TOKEN_EXPIRATION = 60L * 24 * 60 * 60 * 1000;

    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    @Override
    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        Set<Role> roles = Set.of(user.getRole());
        return buildToken(claims, user.getEmail(), roles, ACCESS_TOKEN_EXPIRATION);
    }

    @Override
    public String generateRefreshToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        Set<Role> roles = Set.of(user.getRole());
        return buildToken(claims, user.getEmail(), roles, REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(Map<String, Object> claims, String username, Set<Role> roles, long expiration) {
        List<String> roleNames = roles.stream().map(Enum::name).toList();
        claims.put("roles", roleNames);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.

                class);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
