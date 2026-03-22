package com.example.backend.cart.mapper;

import com.example.backend.cart.dto.CartItemResponse;
import com.example.backend.cart.dto.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart))")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "product.price")
    CartItemResponse toItemResponse(CartItem item);

    default Double calculateTotalPrice(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0.0;
        }

        BigDecimal total = cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = item.getProduct() != null && item.getProduct().getPrice() != null
                            ? item.getProduct().getPrice()
                            : BigDecimal.ZERO;
                    Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.doubleValue();
    }
}
