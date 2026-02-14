package com.example.backend.seller.Controller;

import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.seller.dto.SellerRequestResponse;
import com.example.backend.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/v1/seller")
@RequiredArgsConstructor
@Tag(name = "Seller", description = "Controller for seller onboarding and admin review endpoints.")
public class SellerController {

    private final SellerService sellerService;

    @Operation(
            summary = "Submit a request to become a seller.",
            description = "The authenticated user's email is extracted from the JWT token (Authentication#getName()). The request accepts multipart/form-data: a required store name and an uploaded verification document; an optional reason/note can be included to explain the application.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SellerRequestResponse> requestSeller(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the requested store name the user wants to register as a seller") @RequestParam String storeName,
            @Parameter(description = "optional message or note explaining why the user wants to become a seller") @RequestParam(required = false) String reason,
            @Parameter(description = "verification document (e.g., ID or KYC document) uploaded as multipart file") @RequestPart MultipartFile document

    ) throws IOException {

        String email = authentication.getName();

        return ResponseEntity.ok(
                sellerService.requestSeller(email, storeName,reason ,document)
        );
    }


    @Operation(
            summary = "List all pending seller requests (ADMIN only).",
            description = "Returns a list of pending seller requests for admin review. This endpoint is secured and should only be accessible by users with ADMIN privileges.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/seller-requests")
    public ResponseEntity<List<SellerRequestResponse>> listPending() {
        return ResponseEntity.ok(sellerService.getPendingRequests());
    }


    @Operation(
            summary = "Approve a seller request (ADMIN only).",
            description = "Approving a request promotes the associated user to ROLE_SELLER, creates or updates the seller profile, marks the request as APPROVED, records the reviewing admin and timestamp, and notifies the user by email.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<MessageResponse> approve(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the id of the seller request to approve") @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(
                sellerService.approveRequest(requestId, authentication.getName())
        );
    }


    @Operation(
            summary = "Reject a seller request (ADMIN only).",
            description = "Rejects the specified seller request, records the rejection reason (if provided), records the reviewing admin and timestamp, and notifies the user by email.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<MessageResponse> reject(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the id of the seller request to reject") @PathVariable Long requestId,
            @Parameter(description = "optional human-readable reason for rejection (stored and emailed to the user)") @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(
                sellerService.rejectRequest(requestId, authentication.getName(), reason)
        );
    }
}