package com.example.backend.Cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartResponse {
    private UUID cartId;
    private List<CartItemResponse> items;
    private Double totalPrice;
}
