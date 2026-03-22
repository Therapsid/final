package com.example.backend.users.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend.users.dto.requests.UpdatePasswordRequest;
import com.example.backend.users.dto.responses.GetProfileResponse;
import com.example.backend.users.dto.responses.UpdateEmailInitiateResponse;
import com.example.backend.users.dto.responses.UpdateEmailResponse;
import com.example.backend.users.dto.responses.UpdateProfileResponse;
import com.example.backend.auth.exception.EmailAlreadyUsedException;
import com.example.backend.users.exception.UserNotFoundException;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.exception.InvalidTokenException;
import com.example.backend.users.entity.Users;
import com.example.backend.users.mapper.UserAccountMapper;
import com.example.backend.users.repository.UsersRepo;
import com.example.backend.users.service.UserAccountService;
import com.example.backend.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private static final String UPDATE_EMAIL_PREFIX = "auth:update_email:";

    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final UserAccountMapper userAccountMapper;

    @Override
    @Transactional
    public MessageResponse updatePassword(UpdatePasswordRequest request, String currentUserEmail) {
        Users user = usersRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepo.save(user);
        return new MessageResponse("Password updated successfully");
    }

    @Override
    @Transactional
    public UpdateProfileResponse updateProfile(String userEmail, String firstName, String lastName, MultipartFile file)
            throws IOException {
        Users user = usersRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }

        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Customers",
                            "public_id", userEmail,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            user.setProfileImageUrl(imageUrl);
        }

        usersRepo.save(user);
        return userAccountMapper.toUpdateProfileResponse(user, "Profile updated successfully");
    }

    @Override
    public GetProfileResponse getUserProfile(String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userAccountMapper.toGetProfileResponse(user);
    }

    @Override
    @Transactional
    public MessageResponse deleteCurrentUser(String email) {
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        usersRepo.delete(user);
        return new MessageResponse(" User Deleted Successfully :( ");
    }

    @Override
    @Transactional
    public UpdateEmailInitiateResponse requestEmailUpdate(String currentEmail, String newEmail) {
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (usersRepo.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyUsedException("The new email is already in use.");
        }

        String token = UUID.randomUUID().toString();
        String value = currentEmail + ":" + newEmail;
        redisTemplate.opsForValue().set(UPDATE_EMAIL_PREFIX + token, value, Duration.ofHours(24));

        String verificationLink = "http://localhost:8080/api/v1/auth/update-email/verify?token=" + token;
        String body = "Hello " + user.getFirstName() + ",\n\n"
                + "Click the link to verify your account:\n" + verificationLink
                + "\n\nIf you did not try to change your email , ignore this email.";
        emailService.sendEmail(newEmail, "Verify your email", "Click to verify: " + body);

        return userAccountMapper.toUpdateEmailInitiateResponse(
                "Verification email sent. Please check your inbox to confirm your new email.",
                newEmail
        );
    }

    @Override
    @Transactional
    public UpdateEmailResponse verifyEmailUpdate(String tokenStr) {
        String value = redisTemplate.opsForValue().get(UPDATE_EMAIL_PREFIX + tokenStr);
        if (value == null) {
            throw new InvalidTokenException("The verification token is invalid or expired.");
        }

        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new InvalidTokenException("Invalid token data.");
        }

        String currentEmail = parts[0];
        String newEmail = parts[1];
        Users user = usersRepo.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEmail(newEmail);
        usersRepo.save(user);
        redisTemplate.delete(UPDATE_EMAIL_PREFIX + tokenStr);

        return userAccountMapper.toUpdateEmailResponse("Email updated successfully", user.getEmail());
    }
}
