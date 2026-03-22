package com.example.backend.product.exception;

public class ProductInactiveException extends RuntimeException {

    public ProductInactiveException(String message) {
        super(message);
    }
}
