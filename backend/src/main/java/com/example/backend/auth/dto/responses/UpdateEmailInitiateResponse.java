package com.example.backend.auth.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateEmailInitiateResponse {

    private String message;

    private String newEmail;
}
