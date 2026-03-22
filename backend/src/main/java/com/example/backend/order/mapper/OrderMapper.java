package com.example.backend.order.mapper;

import com.example.backend.order.dto.response.OrderItemResponse;
import com.example.backend.order.dto.response.OrderResponse;
import com.example.backend.order.dto.response.OrderSummaryResponse;
import com.example.backend.order.entity.Order;
import com.example.backend.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    OrderResponse toDto(Order order);

    OrderSummaryResponse toSummaryDto(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toItemDto(OrderItem item);
}