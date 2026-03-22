package com.example.backend.auth.controller;

import com.example.backend.auth.dto.requests.RefreshTokenRequest;
import com.example.backend.auth.dto.requests.ResetPasswordRequest;
import com.example.backend.auth.dto.requests.SignInRequest;
import com.example.backend.auth.dto.requests.SignUpRequest;
import com.example.backend.auth.dto.responses.ForgetPasswordResponse;
import com.example.backend.auth.dto.responses.LoginResponse;
import com.example.backend.auth.dto.responses.RegisterResponse;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.users.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "All endpoints (register, login, refresh, reset password, etc.)")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user.",
            description = "Accepts multipart/form-data with user info and optional profile image."
    )
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @Parameter(description = "User first name", required = true)
            @RequestParam String firstName,
            @Parameter(description = "User last name", required = true)
            @RequestParam String lastName,
            @Parameter(description = "User email", required = true)
            @RequestParam String email,
            @Parameter(description = "User password", required = true)
            @RequestParam String password,
            @Parameter(description = "User role", required = true)
            @RequestParam Role role,
            @Parameter(description = "Optional profile image file")
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        SignUpRequest dto = new SignUpRequest(firstName, lastName, email, password, role);
        RegisterResponse response = authService.register(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Verify email for a newly registered user.",
            description = "Confirms the supplied verification token and activates the user account if valid."
    )
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Parameter(description = "verification token that was sent to the user's email")
            @RequestParam("token") @NotBlank String token
    ) {
        MessageResponse resp = authService.verifyEmail(token);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Authenticate a user (login).",
            description = "Validates credentials and returns login information (tokens/user info)."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "the sign-in request containing email and password")
            @Valid @RequestBody SignInRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh authentication tokens using a refresh token.",
            description = "Accepts a RefreshTokenRequest (containing the refresh token) and returns a new set of tokens."
    )

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @Parameter(description = "refresh token request containing the refresh token") @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Log out the current user and blacklist the token.",
            description = "Blacklists the current access token (and optional refresh token) in Redis until expiry.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Parameter(hidden = true) jakarta.servlet.http.HttpServletRequest request,
            @Parameter(description = "Optional refresh token to revoke") @RequestParam(required = false) String refreshToken) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        return ResponseEntity.ok(authService.logout(accessToken, refreshToken));
    }

    @Operation(
            summary = "Initiate the 'forgot password' flow.",
            description = "Sends a password-reset token/link to the provided email if the account exists."
    )

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgetPasswordResponse> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "map containing 'email' key",
                    content = @Content(schema = @Schema(example = "{\"email\": \"user@example.com\"}"))
            )
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }

    @Operation(
            summary = "Reset password using a reset token.",
            description = "Consumes a ResetPasswordRequest that includes the token and the new password."
    )

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Parameter(description = "reset password request containing token and new password") @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }


}
