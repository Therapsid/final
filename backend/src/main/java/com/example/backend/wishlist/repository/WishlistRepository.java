package com.example.backend.wishlist.repository;

import com.example.backend.users.entity.Users;
import com.example.backend.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByUser(Users user);
}
