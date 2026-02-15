package com.example.backend.exception;

import com.example.backend.category.exception.*;
import com.example.backend.order.exception.InsufficientStockException;
import com.example.backend.order.exception.OrderAlreadyPaidException;
import com.example.backend.order.exception.OrderCancellationException;
import com.example.backend.order.exception.OrderNotFoundException;
import com.example.backend.product.exception.*;
import com.example.backend.wishlist.exception.WishlistNotFoundException;
import com.example.backend.auth.dto.responses.MessageResponse;
import com.example.backend.auth.exception.AccountNotVerifiedException;
import com.example.backend.auth.exception.EmailAlreadyUsedException;
import com.example.backend.auth.exception.InvalidCredentialsException;
import com.example.backend.auth.exception.InvalidOtpException;
import com.example.backend.payment.exception.*;
import com.example.backend.seller.exception.SellerRequestException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j; // Import Lombok Slf4j
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for the entire backend application.
 *
 * Handles exceptions from all modules including Auth, Seller, Payment, Product, Order, Wishlist, and Category.
 * Returns consistent HTTP responses with status, timestamp, error type, and message.
 */
@SuppressWarnings("ALL")
@RestControllerAdvice
@Slf4j // Enables 'log' variable automatically
public class GlobalExceptionHandler {


    // ----------------- Seller module exceptions ----------------- //

    @ExceptionHandler(SellerRequestException.class)
    public ResponseEntity<Map<String, Object>> handleSellerRequestException(SellerRequestException ex) {
        log.warn("Seller request failed: {}", ex.getMessage()); // WARN: Business logic error

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Seller Request Error"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ----------------- Payment module exceptions ----------------- //

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handleGenericPayment(PaymentException ex) {
        log.error("Generic payment error occurred: {}", ex.getMessage(), ex); // ERROR: Unexpected payment issue

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Payment Error"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(StripeOperationException.class)
    public ResponseEntity<Map<String, Object>> handleStripeOperation(StripeOperationException ex) {
        log.error("Stripe gateway operation failed: {}", ex.getMessage(), ex); // ERROR: External system failure

        Map<String, Object> body = Map.of(
                "status", 502,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Gateway"
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(PaymentNotCompletedException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotCompleted(PaymentNotCompletedException ex) {
        log.warn("Payment verification failed/incomplete: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(OrderPaymentNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderPaymentNotAllowed(OrderPaymentNotAllowedException ex) {
        log.warn("Invalid payment attempt for order: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment record not found: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ----------------- Order module exceptions ----------------- //

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order lookup failed: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Order failed due to insufficient stock: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OrderAlreadyPaidException.class)
    public ResponseEntity<Map<String, Object>> handleOrderAlreadyPaid(OrderAlreadyPaidException ex) {
        log.warn("Duplicate payment attempt: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCancellation(OrderCancellationException ex) {
        log.warn("Order cancellation rejected: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    // ----------------- Wishlist module exceptions ----------------- //

    @ExceptionHandler(WishlistNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWishlistNotFound(WishlistNotFoundException ex) {
        log.warn("Wishlist not found: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }


    // ----------------- Product module exceptions ----------------- //

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Product lookup failed: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleProductAlreadyExists(ProductAlreadyExistsException ex) {
        log.warn("Product creation failed, already exists: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidProduct(InvalidProductException ex) {
        log.warn("Invalid product data: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ProductInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleProductInactive(ProductInactiveException ex) {
        log.warn("Access to inactive product denied: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 403,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<Map<String, Object>> handleProductOutOfStock(ProductOutOfStockException ex) {
        log.warn("Product out of stock: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ProductOwnershipException.class)
    public ResponseEntity<Map<String,Object>> handleOwnership(ProductOwnershipException ex) {
        log.warn("Unauthorized product modification attempt: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 403,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    // ----------------- Category module exceptions ----------------- //

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Category not found: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 404,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({CategoryAlreadyExistsException.class, InvalidCategoryException.class, CategoryUpdateException.class, CategoryDeletionException.class})
    public ResponseEntity<Map<String, Object>> handleCategoryBusinessExceptions(RuntimeException ex) {
        log.warn("Category operation failed: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }


    // ----------------- Auth module exceptions ----------------- //

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        log.warn("Registration failed, email already used: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 409,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Conflict"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token usage detected: {}", ex.getMessage());

        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Bad Request"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwt(HttpServletRequest req) {
        log.warn("JWT expired for request: {}", req.getRequestURI());

        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", "Token expired",
                "path", req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleBadCreds(InvalidCredentialsException ex, HttpServletRequest req) {
        // IMPORTANT: Never log the password or sensitive credentials!
        log.warn("Authentication failed (Bad Credentials) for path: {}", req.getRequestURI());

        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage(),
                "path", req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<?> handleInvalidOtp(InvalidOtpException ex) {
        log.warn("Invalid OTP attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new MessageResponse(ex.getMessage()));
    }


    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<?> handleAccountNotVerified(AccountNotVerifiedException ex) {
        log.warn("Unverified account login attempt: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Illegal argument detected: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // fallback - catches everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        // Critical: Log the full stack trace here because this is unexpected
        log.error("Unhandled internal server error: {}", ex.getMessage(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage());
    }

    private ResponseEntity<?> build(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
        return ResponseEntity.status(status).body(body);
    }
}