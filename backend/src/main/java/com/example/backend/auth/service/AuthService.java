package com.example.backend.auth.service;

import com.example.backend.auth.dto.requests.*;
import com.example.backend.auth.dto.responses.ForgetPasswordResponse;
import com.example.backend.auth.dto.responses.LoginResponse;
import com.example.backend.auth.dto.responses.RegisterResponse;
import com.example.backend.common.dto.MessageResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public interface AuthService {
    RegisterResponse register(SignUpRequest signUpRequest, MultipartFile file) throws IOException;

    LoginResponse login(SignInRequest signInRequest);

    ForgetPasswordResponse forgotPassword(String email);

    MessageResponse resetPassword(ResetPasswordRequest request);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    MessageResponse logout(String accessToken, String refreshToken);

    MessageResponse verifyEmail(String token);
}
