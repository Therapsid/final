package com.example.backend.users.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateEmailInitiateResponse {

    private String message;
    private String newEmail;
}
