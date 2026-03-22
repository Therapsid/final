package com.example.backend.users.service;

import com.example.backend.users.dto.requests.UpdatePasswordRequest;
import com.example.backend.users.dto.responses.GetProfileResponse;
import com.example.backend.users.dto.responses.UpdateEmailInitiateResponse;
import com.example.backend.users.dto.responses.UpdateEmailResponse;
import com.example.backend.users.dto.responses.UpdateProfileResponse;
import com.example.backend.common.dto.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserAccountService {

    UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName, MultipartFile file) throws IOException;

    GetProfileResponse getUserProfile(String email);

    MessageResponse deleteCurrentUser(String email);

    UpdateEmailInitiateResponse requestEmailUpdate(String currentEmail, String newEmail);

    UpdateEmailResponse verifyEmailUpdate(String tokenStr);

    MessageResponse updatePassword(UpdatePasswordRequest request, String currentUserEmail);
}
