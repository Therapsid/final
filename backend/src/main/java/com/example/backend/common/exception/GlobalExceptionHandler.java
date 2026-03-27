package com.example.backend.common.exception;

import com.example.backend.category.exception.*;
import com.example.backend.order.exception.InsufficientStockException;
import com.example.backend.order.exception.OrderAlreadyPaidException;
import com.example.backend.order.exception.OrderCancellationException;
import com.example.backend.order.exception.OrderNotFoundException;
import com.example.backend.product.exception.*;
import com.example.backend.wishlist.exception.WishlistNotFoundException;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.auth.exception.AccountNotVerifiedException;
import com.example.backend.auth.exception.EmailAlreadyUsedException;
import com.example.backend.auth.exception.InvalidCredentialsException;
import com.example.backend.auth.exception.InvalidOtpException;
import com.example.backend.payment.exception.*;
import com.example.backend.seller.exception.SellerRequestException;
import com.example.backend.users.exception.UserNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("ALL")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SellerRequestException.class)
    public ResponseEntity<Map<String, Object>> handleSellerRequestException(SellerRequestException ex) {
        log.warn("Seller request failed: {}", ex.getMessage());
        Map<String, Object> body = Map.of(
                "status", 400,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Seller Request Error"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handleGenericPayment(PaymentException ex) {
        log.error("Generic payment error occurred: {}", ex.getMessage(), ex);
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
        log.error("Stripe gateway operation failed: {}", ex.getMessage(), ex);
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
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
    public ResponseEntity<Map<String, Object>> handleOwnership(ProductOwnershipException ex) {
        log.warn("Unauthorized product modification attempt: {}", ex.getMessage());
        Map<String, Object> body = Map.of(
                "status", 403,
                "timestamp", LocalDateTime.now(),
                "message", ex.getMessage(),
                "error", "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

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
        Map<String, Object> body = Map.of(
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
        log.warn("Authentication failed (Bad Credentials) for path: {}", req.getRequestURI());
        Map<String, Object> body = Map.of(
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Request body validation failed: {}", errors);
        return buildValidation(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn("Method validation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        log.warn("Constraint validation failed: {}", errors);
        return buildValidation(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Malformed or missing request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
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

    private ResponseEntity<?> buildValidation(HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("errors", errors);
        return ResponseEntity.status(status).body(body);
    }
}
