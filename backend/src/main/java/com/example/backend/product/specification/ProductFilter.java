package com.example.backend.product.specification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ProductFilter {

    private UUID id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
