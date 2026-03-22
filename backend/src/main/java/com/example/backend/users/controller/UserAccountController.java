package com.example.backend.users.controller;

import com.example.backend.common.dto.MessageResponse;
import com.example.backend.users.dto.requests.UpdateEmailRequest;
import com.example.backend.users.dto.requests.UpdatePasswordRequest;
import com.example.backend.users.dto.responses.GetProfileResponse;
import com.example.backend.users.dto.responses.UpdateEmailInitiateResponse;
import com.example.backend.users.dto.responses.UpdateEmailResponse;
import com.example.backend.users.dto.responses.UpdateProfileResponse;
import com.example.backend.users.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Account", description = "Endpoints for managing authenticated user account")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Operation(
            summary = "Get current user profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<GetProfileResponse> me(@Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(userAccountService.getUserProfile(authentication.getName()));
    }

    @Operation(
            summary = "Update current user profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(
                userAccountService.updateProfile(authentication.getName(), firstName, lastName, file)
        );
    }

    @Operation(
            summary = "Delete current user account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteMe(@Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(userAccountService.deleteCurrentUser(authentication.getName()));
    }

    @Operation(
            summary = "Request email update.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/me/email")
    public ResponseEntity<UpdateEmailInitiateResponse> requestEmailUpdate(
            @Valid @RequestBody UpdateEmailRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.accepted()
                .body(userAccountService.requestEmailUpdate(authentication.getName(), request.getNewEmail()));
    }

    @Operation(
            summary = "Verify email update token."
    )
    @GetMapping("/me/email/verify")
    public ResponseEntity<UpdateEmailResponse> verifyEmailUpdate(
            @RequestParam("token") @NotBlank String token
    ) {
        return ResponseEntity.ok(userAccountService.verifyEmailUpdate(token));
    }

    @Operation(
            summary = "Update current user password.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(userAccountService.updatePassword(request, authentication.getName()));
    }
}
