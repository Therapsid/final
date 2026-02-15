package com.example.backend.auth.dto.responses;

import lombok.Data;

@Data

public class JwtAuthenticationResponse {
    private String token;
    private String refreshToken;
}
