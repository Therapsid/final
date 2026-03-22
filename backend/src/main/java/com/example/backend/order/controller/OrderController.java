package com.example.backend.order.controller;

import com.example.backend.order.dto.request.CreateOrderRequest;
import com.example.backend.order.dto.request.DirectOrderRequest;
import com.example.backend.order.dto.response.OrderResponse;
import com.example.backend.order.dto.response.OrderSummaryResponse;
import com.example.backend.order.entity.Order;
import com.example.backend.order.mapper.OrderMapper;
import com.example.backend.order.service.OrderService;
import com.example.backend.auth.dto.responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Endpoints for managing user orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create an order from the current user's cart.",
            description = "Create an order from the current user's cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "contains the shipping address for the order") @Valid @RequestBody CreateOrderRequest req,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Order order = orderService.createOrderFromCart(userEmail, req.getShippingAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toDto(order));
    }

    @Operation(
            summary = "Create a direct order for a single product without adding it to the cart.",
            description = "Create a direct order for a single product without adding it to the cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/direct")
    public ResponseEntity<OrderResponse> directOrder(
            @Parameter(description = "contains productId, quantity, and shipping address") @Valid @RequestBody DirectOrderRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Order order = orderService.createDirectOrder(
                userEmail,
                request.getProductId(),
                request.getQuantity(),
                request.getShippingAddress()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toDto(order));
    }

    @Operation(
            summary = "Get a paginated list of orders for the authenticated user.",
            description = "Get a paginated list of orders for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Page<Order> page = orderService.getOrdersForUser(userEmail, pageable);
        Page<OrderSummaryResponse> dtoPage = page.map(OrderMapper::toSummaryDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(
            summary = "Get a single order by its ID for the authenticated user.",
            description = "Get a single order by its ID for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "the Long of the order") @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Order order = orderService.getOrderById(id, userEmail);
        return ResponseEntity.ok(OrderMapper.toDto(order));
    }

    @Operation(
            summary = "Cancel an order by its ID for the authenticated user.",
            description = "Business rules: - Can only cancel if order status is CREATED or PENDING_PAYMENT - Cannot cancel if the order is PAID, SHIPPED, DELIVERED",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelOrder(
            @Parameter(description = "the Long of the order") @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String userEmail = authentication.getName();
        orderService.cancelOrder(id, userEmail);
        return ResponseEntity.ok(new MessageResponse("Order cancelled."));
    }
}
