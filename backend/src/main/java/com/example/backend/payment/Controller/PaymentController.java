package com.example.backend.payment.Controller;

import com.example.backend.payment.dto.PaymentConfirmDto;
import com.example.backend.payment.dto.PaymentCreateResponse;
import com.example.backend.payment.dto.RefundRequest;
import com.example.backend.payment.service.PaymentService;
import com.stripe.model.Refund;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Endpoints for processing payments and refunds via Stripe")
public class PaymentController {

    private final PaymentService paymentService;


    @Operation(
            summary = "Create a Stripe checkout session for a specific order.",
            description = "Create a Stripe checkout session for a specific order.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/create/{orderId}")
    public PaymentCreateResponse createPayment(
            @Parameter(description = "the ID of the order to pay") @PathVariable Long orderId,
            @Parameter(hidden = true) Authentication authentication) {

        return paymentService.createCheckoutSessionForOrder(
                orderId,
                authentication.getName()
        );
    }


    @Operation(
            summary = "Confirm the payment status of a Stripe checkout session.",
            description = "Confirm the payment status of a Stripe checkout session.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/confirm")
    public PaymentConfirmDto confirmPayment(
            @Parameter(description = "the Stripe checkout session ID to confirm") @RequestParam("session_id") String sessionId,
            @Parameter(hidden = true) Authentication authentication) {

        return paymentService.confirmPaymentBySessionId(
                sessionId,
                authentication.getName()
        );
    }

    @Operation(
            summary = "Process a refund for a paid order.",
            description = "Allows the authenticated user to request a full or partial refund for an order they own. The refund is processed through Stripe, and the local payment record is updated accordingly.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/refund")
    public ResponseEntity<?> refund(
            @Parameter(description = "the refund request containing order ID and optional refund amount") @RequestBody RefundRequest req,
            @Parameter(hidden = true) Authentication auth) {
        // validate ownership
        Refund refund = paymentService.refundPaymentForOrder(req, auth.getName());
        return ResponseEntity.ok(Map.of("refundId", refund.getId(), "status", refund.getStatus()));
    }
}