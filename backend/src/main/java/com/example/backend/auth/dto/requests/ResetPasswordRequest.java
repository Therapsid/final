package com.example.backend.auth.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {

    private String email;

    @NotBlank(message = "otp is required")
    private String otp;

    @NotBlank(message = "New password is required")

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
