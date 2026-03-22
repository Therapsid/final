package com.example.backend.product.mapper;

import com.example.backend.product.dto.ProductResponse;
import com.example.backend.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "message", source = "message")
    @Mapping(target = "categoryId", source = "product.category.id")
    @Mapping(target = "categoryName", source = "product.category.name")
    ProductResponse toResponse(Product product, String message);
}
