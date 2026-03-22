package com.example.backend.auth.service;

import com.example.backend.auth.dto.requests.*;
import com.example.backend.auth.dto.responses.*;
import com.example.backend.users.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public interface AuthService {
    RegisterResponse register(SignUpRequest signUpRequest, MultipartFile file) throws IOException;

    LoginResponse login(SignInRequest signInRequest);

    UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName, MultipartFile file) throws IOException;

    GetProfileResponse getUserProfile(String email);

    MessageResponse deleteCurrentUser(String email);

    UpdateEmailInitiateResponse requestEmailUpdate(String currentEmail, String newEmail);

    UpdateEmailResponse verifyEmailUpdate(String tokenStr);

    ForgetPasswordResponse forgotPassword(String email);

    MessageResponse resetPassword(ResetPasswordRequest request);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    MessageResponse logout(String accessToken, String refreshToken);

    MessageResponse updatePassword(UpdatePasswordRequest request, String currentUserEmail);

    MessageResponse verifyEmail(String token);


}
