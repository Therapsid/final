package com.example.backend.order.dto.response;

import com.example.backend.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Builder
public class OrderSummaryResponse {

    private Long id;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private LocalDateTime createdAt;
}
