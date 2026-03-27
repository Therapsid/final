package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.requests.*;
import com.example.backend.auth.dto.responses.ForgetPasswordResponse;
import com.example.backend.auth.dto.responses.LoginResponse;
import com.example.backend.auth.dto.responses.RegisterResponse;
import com.example.backend.auth.exception.*;
import com.example.backend.auth.service.AuthService;
import com.example.backend.auth.service.TokenBlacklistService;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.common.exception.InvalidTokenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.common.exception.TooManyRequestsException;
import com.example.backend.users.entity.Role;
import com.example.backend.users.entity.Users;
import com.example.backend.users.exception.UserNotFoundException;
import com.example.backend.users.repository.UsersRepo;
import com.example.backend.common.util.CloudinaryService;
import com.example.backend.common.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final AuthenticationManager authenticationManager;
    private final JWTserviceImpl jwtService;
    private final StringRedisTemplate redisTemplate;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String RESET_ATTEMPT_PREFIX = "auth:reset_attempt:";
    private static final String OTP_PREFIX = "auth:otp:";
    private static final String VERIFY_EMAIL_PREFIX = "auth:verify_email:";

    @Override
    @Transactional
    public RegisterResponse register(SignUpRequest signUpRequest, MultipartFile file) throws IOException {
        if (usersRepo.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("The new email is already in use.");
        }

        Role userRole = signUpRequest.getRole() != null ? signUpRequest.getRole() : Role.ROLE_USER;
        Users user = Users.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .role(userRole)
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(file, "Customers", signUpRequest.getEmail());
        }

        if (imageUrl != null) {
            user.setProfileImageUrl(imageUrl);
        }

        usersRepo.save(user);

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(VERIFY_EMAIL_PREFIX + token, user.getEmail(), Duration.ofHours(24));

        String verificationLink = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Click the link to verify your account:\n" + verificationLink +
                "\n\nIf you did not register, ignore this email.";

        emailService.sendEmail(user.getEmail(), "Verify your account", body);
        return new RegisterResponse("User registered. Please check your email for verification.", token);
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        String email = redisTemplate.opsForValue().get(VERIFY_EMAIL_PREFIX + token);
        if (email == null) {
            return new MessageResponse("Token invalid or expired");
        }

        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEmailVerified(true);
        user.setEnabled(true);
        usersRepo.save(user);

        redisTemplate.delete(VERIFY_EMAIL_PREFIX + token);
        return new MessageResponse("Email verified successfully!");
    }

    @Override
    public LoginResponse login(SignInRequest signInRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),
                            signInRequest.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        Users user = usersRepo.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Please verify your email first");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponse(
                "Login successful",
                token, refreshToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getToken();
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException("Refresh token has been revoked (blacklisted). Please login again.");
        }

        String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if (!jwtService.validateToken(refreshToken, user)) {
            throw new InvalidTokenException("Refresh token expired or invalid");
        }

        String newAccessToken = jwtService.generateToken(user);
        return new LoginResponse(
                "Token refreshed successfully",
                newAccessToken,
                refreshToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileImageUrl(),
                user.getRole()
        );
    }

    @Override
    public MessageResponse logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                java.util.Date expiration = jwtService.extractExpiration(accessToken);
                long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                if (ttl > 0) {
                    tokenBlacklistService.blacklistToken(accessToken, ttl);
                }

            } catch (Exception e) {
            }
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                java.util.Date expiration = jwtService.extractExpiration(refreshToken);
                long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                if (ttl > 0) {
                    tokenBlacklistService.blacklistToken(refreshToken, ttl);
                }

            } catch (Exception e) {
            }
        }

        return new MessageResponse("Logged out successfully");
    }

    @Transactional
    @Override
    public ForgetPasswordResponse forgotPassword(String currentEmail) {
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User not found with email: " + currentEmail));
        String rateLimitKey = RESET_ATTEMPT_PREFIX + currentEmail;
        String attemptsStr = redisTemplate.opsForValue().get(rateLimitKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= 3) {
            throw new TooManyRequestsException("Too many reset attempts. Try again later.");
        }

        redisTemplate.opsForValue().increment(rateLimitKey);
        if (attempts == 0) {
            redisTemplate.expire(rateLimitKey, Duration.ofHours(1));
        }

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        String otpKey = OTP_PREFIX + currentEmail;
        redisTemplate.opsForValue().set(otpKey, otp, Duration.ofMinutes(15));
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Your password reset code (OTP) is: " + otp + "\n\n" +
                "This code expires in 15 minutes.";
        emailService.sendEmail(user.getEmail(), "Password Reset OTP", body);
        return new ForgetPasswordResponse("OTP sent successfully. Please check your inbox.");
    }

    @Transactional
    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        Users user = usersRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        String otpKey = OTP_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        if (storedOtp == null) {
            throw new InvalidOtpException("OTP expired or invalid");
        }

        if (!storedOtp.equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepo.save(user);
        redisTemplate.delete(otpKey);
        redisTemplate.delete(RESET_ATTEMPT_PREFIX + request.getEmail());
        return new MessageResponse("Password reset successfully");
    }
}
