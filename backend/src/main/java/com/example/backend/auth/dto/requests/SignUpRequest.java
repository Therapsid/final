package com.example.backend.auth.dto.requests;


import com.example.backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    private Role role;
//    private MultipartFile profileImage;
}
