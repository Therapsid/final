package com.example.backend.auth.dto.requests;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    private String token;
}
