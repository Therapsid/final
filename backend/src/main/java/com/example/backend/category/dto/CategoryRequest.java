package com.example.backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be at most 50 characters")
    private String name;

    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;

    private Long parentId;
}
