package com.example.backend.wishlist.mapper;

import com.example.backend.product.entity.Product;
import com.example.backend.wishlist.dto.ProductDto;
import com.example.backend.wishlist.dto.WishlistResponse;
import com.example.backend.wishlist.entity.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WishlistMapper {

    @Mapping(target = "wishlistId", source = "id")
    WishlistResponse toResponse(Wishlist wishlist);

    @Mapping(target = "price", source = "price", qualifiedByName = "toDouble")
    ProductDto toProductDto(Product product);

    @Named("toDouble")
    default Double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}
