package com.example.backend.Order.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Valid
public class DirectOrderRequest {

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;

    @NotNull
    private String shippingAddress;
}
