package com.example.backend.cart.service;
import com.example.backend.cart.dto.CartResponse;
import com.example.backend.common.dto.MessageResponse;
import java.util.UUID;

public interface CartService {
    CartResponse addToCart(String userEmail, UUID productId, Integer quantity);
    CartResponse updateQuantity(String userEmail, UUID productId, Integer quantity);
    CartResponse removeFromCart(String userEmail, UUID productId);
    CartResponse getCart(String userEmail);
    MessageResponse clearCart(String userEmail);
}
