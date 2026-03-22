package com.example.backend.auth.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateEmailRequest {
    @Email(message = "Invalid email format")

    @NotBlank(message = "New email is required")
    private String newEmail;
}
