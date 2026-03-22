package com.example.backend.users.service;

import com.example.backend.users.entity.Users;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UsersServices extends UserDetailsService {

    List<Users> getAllUsers();
}
