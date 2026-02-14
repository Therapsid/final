package com.example.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned when creating a Stripe Checkout session.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateResponse {
    private String sessionId;
    private String url;


    public static PaymentCreateResponse of(String sessionId, String url) {
        return new PaymentCreateResponse(sessionId, url);
    }

}