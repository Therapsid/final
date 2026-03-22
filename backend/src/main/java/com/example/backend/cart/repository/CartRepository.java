package com.example.backend.cart.repository;

import com.example.backend.cart.entity.Cart;
import com.example.backend.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(Users user);
}
