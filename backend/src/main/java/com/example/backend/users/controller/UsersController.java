package com.example.backend.users.controller;

import com.example.backend.users.entity.Users;
import com.example.backend.users.service.UsersServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
public class UsersController {

    private final UsersServices userService;

    @Operation(
            summary = "Development/test endpoint: list all users.",
            description = "Exposes all users in the system — intended for dev/test only and should be removed or secured for production."
    )

    @GetMapping("/dev/users")
    public ResponseEntity<List<Users>> listUsers() {
        return ResponseEntity.ok((userService).getAllUsers());
    }
}
