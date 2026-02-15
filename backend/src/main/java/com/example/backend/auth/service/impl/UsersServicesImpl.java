package com.example.backend.auth.service.impl;

import com.example.backend.auth.service.UsersServices;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServicesImpl implements UsersServices {


    private final UsersRepo userRepo;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name()) // important: ROLE_ prefix
                .build();
    }
}
