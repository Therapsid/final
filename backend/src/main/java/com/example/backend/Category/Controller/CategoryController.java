package com.example.backend.Category.Controller;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.service.CategoryService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
@Tag(name = "Category", description = "Endpoints for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;


    @Operation(
            summary = "Create a new category (ADMIN only).",
            description = "Create a new category (ADMIN only).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "the category data to create") @RequestBody @Valid CategoryRequest categoryRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequest));
    }

    @Operation(
            summary = "Get a category by its ID.",
            description = "Get a category by its ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "the ID of the category to retrieve") @PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    @Operation(
            summary = "Get all categories.",
            description = "Get all categories."
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
            summary = "Update an existing category (ADMIN only).",
            description = "Update an existing category (ADMIN only).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "the ID of the category to update") @PathVariable Long id,
            @Parameter(description = "the new category data") @RequestBody @Valid CategoryRequest categoryRequest){
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequest));
    }


    @Operation(
            summary = "Delete a category by ID (ADMIN only).",
            description = "Delete a category by ID (ADMIN only).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCategory(
            @Parameter(description = "the ID of the category to delete") @PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}