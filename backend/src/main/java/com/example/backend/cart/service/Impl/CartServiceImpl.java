package com.example.backend.cart.service.Impl;
import com.example.backend.cart.dto.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.entity.CartItem;
import com.example.backend.cart.mapper.CartMapper;
import com.example.backend.cart.repository.CartItemRepository;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.cart.service.CartService;
import com.example.backend.product.entity.Product;
import com.example.backend.product.exception.ProductNotFoundException;
import com.example.backend.product.exception.ProductOutOfStockException;
import com.example.backend.product.repository.ProductRepository;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.users.exception.UserNotFoundException;
import com.example.backend.users.entity.Users;
import com.example.backend.users.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@SuppressWarnings("ALL")
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse addToCart(String userEmail, UUID productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductOutOfStockException("Product is not active");
        }

        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return cartRepository.save(c);
        });
        
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();
        
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new ProductOutOfStockException("Not enough stock to increase quantity to " + newQty);
            }

            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
            cartRepository.save(cart);
        }

        return cartMapper.toResponse(cart);
    }

    @Override

    @Transactional
    public CartResponse updateQuantity(String userEmail, UUID productId, Integer quantity) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found in cart: " + productId));
        if (quantity == null || quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
            return cartMapper.toResponse(cart);
        }

        Product product = item.getProduct();
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return cartMapper.toResponse(cart);
    }

    @Override

    @Transactional
    public CartResponse removeFromCart(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }

        return cartMapper.toResponse(cart);
    }

    @Override

    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return c;
        });
        return cartMapper.toResponse(cart);
    }

    @Override

    @Transactional
    public MessageResponse clearCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        cartRepository.findByUser(user).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
        return new MessageResponse(" Cart cleared successfully.");
    }
}
