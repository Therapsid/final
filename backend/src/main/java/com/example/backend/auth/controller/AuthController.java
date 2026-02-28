package com.example.backend.auth.controller;

import com.example.backend.auth.dto.requests.*;
import com.example.backend.auth.dto.responses.*;
import com.example.backend.auth.service.AuthService;
import com.example.backend.entity.Role;
import com.example.backend.entity.Users;
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
            description = "Accepts multipart/form-data so the client can optionally upload a profile picture."
    )
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @Valid @ModelAttribute SignUpRequest dto,
            @Parameter(description = "optional profile image file")
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        RegisterResponse response = authService.register(dto, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Verify email for a newly registered user.",
            description = "Confirms the supplied verification token and activates the user account if valid."
    )
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Parameter(description = "verification token that was sent to the user's email") @RequestParam("token") @NotBlank String token) {
        MessageResponse resp = authService.verifyEmail(token);
        return ResponseEntity.ok(resp);
    }


    @Operation(
            summary = "Authenticate a user (login).",
            description = "Validates credentials and returns login information (tokens/user info)."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "the sign-in request containing email and password") @Valid @RequestBody SignInRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Refresh authentication tokens using a refresh token.",
            description = "Accepts a RefreshTokenRequest (containing the refresh token) and returns a new set of tokens."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @Parameter(description = "refresh token request containing the refresh token") @Valid @RequestBody RefreshTokenReq request) {
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
            summary = "Update the current user's profile.",
            description = "Allows updating the first / last name and an optional profile picture. The authenticated user's identity is taken from the Security Authentication principal (email).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "optional new first name") @RequestParam(required = false) String firstName,
            @Parameter(description = "optional new last name") @RequestParam(required = false) String lastName,
            @Parameter(description = "optional new profile image file") @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        String TokenEmail = authentication.getName(); // Extract user identity from a token

        UpdateProfileResponse response = authService.updateProfile(
                TokenEmail,
                firstName,
                lastName,
                file
        );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get the currently authenticated user's profile.",
            description = "Uses the authenticated principal to fetch the user's profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<GetProfileResponse> getCurrentUser(@Parameter(hidden = true) Authentication authentication) {
        GetProfileResponse resp= authService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(resp);
    }


    @Operation(
            summary = "Delete the currently authenticated user's account.",
            description = "Permanently deletes the user tied to the authenticated principal (email).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> DeleteCurrentUser(@Parameter(hidden = true) Authentication authentication){
        MessageResponse resp = authService.deleteCurrentUser(authentication.getName());
        return ResponseEntity.ok(resp);
    }


    @Operation(
            summary = "Request an email update for the current user.",
            description = "Triggers an email verification flow to change the user's email. The service will send a verification link/token to the new email address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/update-email")
    public ResponseEntity<UpdateEmailInitiateResponse> requestEmailUpdate(
            @Parameter(description = "contains the new email address") @Valid @RequestBody UpdateEmailRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        UpdateEmailInitiateResponse resp = authService.requestEmailUpdate(authentication.getName(), request.getNewEmail());
        return ResponseEntity.accepted().body(resp);
    }


    @Operation(
            summary = "Verify updated email address using token.",
            description = "Confirms the token that was sent to the new email address and performs the actual update."
    )
    @GetMapping("/update-email/verify")
    public ResponseEntity<UpdateEmailResponse> verifyUpdatedEmail(
            @Parameter(description = "verification token") @RequestParam("token") @NotBlank String token) {
        UpdateEmailResponse resp = authService.verifyEmailUpdate(token);
        return ResponseEntity.ok(resp);
    }


    @Operation(
            summary = "Update the current user's password.",
            description = "Allows authenticated users to change their password by supplying old/new credentials (as defined in UpdatePasswordRequest).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/update-password")
    public ResponseEntity<MessageResponse> updatePassword(
            @Parameter(description = "update password request containing current and new password") @Valid @RequestBody UpdatePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        MessageResponse resp = authService.updatePassword(request, authentication.getName());
        return ResponseEntity.ok(resp);
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
            @RequestBody Map<String,String> body) {
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

    @Operation(
            summary = "Development/test endpoint: list all users.",
            description = "Exposes all users in the system — intended for dev/test only and should be removed or secured for production."
    )
    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok((authService).getAllUsers());
    }

}