package com.example.backend.order.exception;


public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message , Long orderId) {
        super(message + orderId);
    }
}
