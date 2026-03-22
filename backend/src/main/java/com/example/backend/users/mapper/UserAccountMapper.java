package com.example.backend.users.mapper;

import com.example.backend.users.dto.responses.GetProfileResponse;
import com.example.backend.users.dto.responses.UpdateEmailInitiateResponse;
import com.example.backend.users.dto.responses.UpdateEmailResponse;
import com.example.backend.users.dto.responses.UpdateProfileResponse;
import com.example.backend.users.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserAccountMapper {

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    GetProfileResponse toGetProfileResponse(Users user);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    UpdateProfileResponse toUpdateProfileResponse(Users user, String message);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "newEmail", source = "newEmail")
    UpdateEmailInitiateResponse toUpdateEmailInitiateResponse(String message, String newEmail);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "newEmail", source = "newEmail")
    UpdateEmailResponse toUpdateEmailResponse(String message, String newEmail);
}
