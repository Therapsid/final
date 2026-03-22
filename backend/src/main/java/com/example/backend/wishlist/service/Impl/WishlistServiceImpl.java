package com.example.backend.wishlist.service.Impl;

import com.example.backend.users.exception.UserNotFoundException;
import com.example.backend.product.entity.Product;
import com.example.backend.product.exception.ProductNotFoundException;
import com.example.backend.product.repository.ProductRepository;
import com.example.backend.users.entity.Users;
import com.example.backend.users.repository.UsersRepo;
import com.example.backend.wishlist.dto.WishlistResponse;
import com.example.backend.wishlist.entity.Wishlist;
import com.example.backend.wishlist.exception.WishlistNotFoundException;
import com.example.backend.wishlist.mapper.WishlistMapper;
import com.example.backend.wishlist.repository.WishlistRepository;
import com.example.backend.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));
        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProducts(new ArrayList<>());
            return wishlistRepository.save(w);
        });
        boolean exists = wishlist.getProducts().stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
        if (!exists) {
            wishlist.getProducts().add(product);
            wishlistRepository.save(wishlist);
        }

        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse removeFromWishlist(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));
        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProducts(new ArrayList<>());
            return w;
        });
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    public WishlistResponse clearWishlist(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));
        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(wishlist);
    }
}
