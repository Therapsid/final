package com.example.backend.seller.mapper;

import com.example.backend.seller.dto.SellerRequestResponse;
import com.example.backend.seller.entity.SellerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SellerMapper {

    DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toIsoString")
    @Mapping(target = "reviewedAt", source = "reviewedAt", qualifiedByName = "toIsoString")
    SellerRequestResponse toDto(SellerRequest request);

    @Named("toIsoString")
    default String toIsoString(LocalDateTime time) {
        return time == null ? null : time.format(ISO);
    }
}
