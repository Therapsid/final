package com.example.backend.category.mapper;

import com.example.backend.category.dto.CategoryResponse;
import com.example.backend.category.entity.Category;
import com.example.backend.product.dto.ProductResponse;
import com.example.backend.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    default CategoryResponse toResponse(Category category, String message) {
        if (category == null) {
            return null;
        }

        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentName = category.getParent() != null ? category.getParent().getName() : null;

        List<CategoryResponse> subCategories =
                (category.getSubCategories() == null ? Collections.<Category>emptyList() : category.getSubCategories())
                        .stream()
                        .map(sub -> toResponse(sub, ""))
                        .toList();

        List<ProductResponse> products =
                (category.getProducts() == null ? Collections.<Product>emptyList() : category.getProducts())
                        .stream()
                        .map(this::toProductResponse)
                        .toList();

        return CategoryResponse.builder()
                .message(message)
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(parentId)
                .parentName(parentName)
                .subCategories(subCategories)
                .products(products)
                .build();
    }

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toProductResponse(Product product);
}
