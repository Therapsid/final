package com.example.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Builder
public class ProductResponse {

    private String message;

    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private Long categoryId;

    private String categoryName;

    private Integer stock;

    private Boolean active;
}
