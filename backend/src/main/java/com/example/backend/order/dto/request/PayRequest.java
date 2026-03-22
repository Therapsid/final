package com.example.backend.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayRequest {

    @NotBlank
    private String phoneNumber;

    private String idempotencyKey;
}
