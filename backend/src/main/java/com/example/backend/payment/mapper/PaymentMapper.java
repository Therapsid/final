package com.example.backend.payment.mapper;

import com.example.backend.payment.dto.PaymentConfirmDto;
import com.example.backend.payment.dto.PaymentCreateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "url", source = "url")
    PaymentCreateResponse toCreateResponse(String sessionId, String url);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "paid", source = "paid")
    PaymentConfirmDto toConfirmResponse(String message, boolean paid);

    default PaymentConfirmDto ok(String message) {
        return toConfirmResponse(message, true);
    }

    default PaymentConfirmDto pending(String message) {
        return toConfirmResponse(message, false);
    }
}
