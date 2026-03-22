package com.example.backend.cart.controller;

import com.example.backend.common.dto.MessageResponse;
import com.example.backend.cart.dto.CartItemRequest;
import com.example.backend.cart.dto.CartResponse;
import com.example.backend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cart", description = "Endpoints for managing user shopping cart")
public class CartController {

    private final CartService cartService;

    @Operation(
            summary = "Add a product to the user's cart.",
            description = "If the user has no existing cart, a new cart is created automatically.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the cart item request containing productId and quantity") @RequestBody @Valid CartItemRequest request) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.addToCart(userEmail, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Update the quantity of an existing cart item.",
            description = "If the quantity is set to 0 or less, the item is removed from the cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateQuantity(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the cart item request containing productId and the new quantity") @RequestBody @Valid CartItemRequest request) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.updateQuantity(userEmail, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Update the quantity of an existing cart item.",
            description = "If the quantity is set to 0 or less, the item is removed from the cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeFromCart(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the cart item request containing productId and the new quantity") @PathVariable UUID productId) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.removeFromCart(userEmail, productId);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Retrieve the current user's cart.",
            description = "Retrieve the current user's cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getCart(@Parameter(hidden = true) Authentication authentication) {
        String userEmail = authentication.getName();
        CartResponse resp = cartService.getCart(userEmail);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Clear all items from the current user's cart.",
            description = "Clear all items from the current user's cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> clearCart(@Parameter(hidden = true) Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(cartService.clearCart(userEmail));
    }
}
