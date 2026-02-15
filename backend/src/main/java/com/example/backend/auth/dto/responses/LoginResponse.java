package com.example.backend.auth.dto.responses;

import com.example.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String accessToken; // JWT token
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private Role role;
}
