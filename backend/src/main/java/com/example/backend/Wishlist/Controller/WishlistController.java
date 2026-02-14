package com.example.backend.Wishlist.Controller;

import com.example.backend.Wishlist.dto.WishlistResponse;
import com.example.backend.Wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Endpoints for managing user wishlist")
public class WishlistController {
    private final WishlistService wishlistService;


    @Operation(
            summary = "Add a product to the authenticated user's wishlist.",
            description = "Add a product to the authenticated user's wishlist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "UUID of the product to add") @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(auth.getName(), productId));
    }


    @Operation(
            summary = "Remove a product from the authenticated user's wishlist.",
            description = "Remove a product from the authenticated user's wishlist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            @Parameter(hidden = true) Authentication auth,
            @Parameter(description = "UUID of the product to remove") @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(auth.getName(), productId));
    }


    @Operation(
            summary = "Retrieve the authenticated user's wishlist.",
            description = "Retrieve the authenticated user's wishlist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(@Parameter(hidden = true) Authentication auth) {
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }


    @Operation(
            summary = "Clear all items from the authenticated user's wishlist.",
            description = "Clear all items from the authenticated user's wishlist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/clear")
    public ResponseEntity<WishlistResponse> clearWishlist(@Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(wishlistService.clearWishlist(email));
    }
}