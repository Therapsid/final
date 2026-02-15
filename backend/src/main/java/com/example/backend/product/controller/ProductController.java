package com.example.backend.product.controller;

import com.example.backend.product.dto.ProductRequest;
import com.example.backend.product.dto.ProductResponse;
import com.example.backend.product.service.ProductService;
import com.example.backend.auth.dto.responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product", description = "Endpoints for managing products (Create, Read, Update, Delete)")
public class ProductController {

    private final ProductService productService;


    @Operation(
        summary = "Create a new product.",
        description = "Accepts multipart/form-data to optionally include a product image. Only accessible by users with SELLER role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "the product name") @RequestParam String name,
            @Parameter(description = "the product description") @RequestParam String description,
            @Parameter(description = "the product price") @RequestParam Double price,
            @Parameter(description = "the ID of the category the product belongs to") @RequestParam Long categoryId,
            @Parameter(description = "the available stock quantity") @RequestParam Integer stock,
            @Parameter(description = "optional product image") @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();
        ProductResponse resp = productService.createProduct(request, file, sellerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    @Operation(
        summary = "Get all products with pagination.",
        description = "Public endpoint that lists products page by page."
    )
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @Operation(
        summary = "Get a product by its ID.",
        description = "Get a product by its ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "the UUID of the product") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @Operation(
        summary = "Update an existing product.",
        description = "Only accessible by the product owner (SELLER) or ADMIN. Accepts multipart/form-data to optionally update product image.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "the UUID of the product") @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "optional new product name") @RequestParam(required = false) String name,
            @Parameter(description = "optional new product description") @RequestParam(required = false) String description,
            @Parameter(description = "optional new price") @RequestParam(required = false) Double price,
            @Parameter(description = "optional new category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "optional new stock quantity") @RequestParam(required = false) Integer stock,
            @Parameter(description = "optional new product image") @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();
        ProductResponse resp = productService.updateProduct(id, request, file, sellerEmail);
        return ResponseEntity.ok(resp);
    }



    @Operation(
        summary = "Delete a product by its ID.",
        description = "Only accessible by the product owner (SELLER) or ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct(
            @Parameter(description = "the UUID of the product") @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication) {

        String sellerEmail = authentication.getName();
        MessageResponse resp = productService.deleteProduct(id, sellerEmail);
        return ResponseEntity.ok(resp);
    }
}