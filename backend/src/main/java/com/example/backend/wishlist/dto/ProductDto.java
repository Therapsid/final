package com.example.backend.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private UUID id;

    private String name;

    private String description;

    private Double price;

    private String imageUrl;
}
