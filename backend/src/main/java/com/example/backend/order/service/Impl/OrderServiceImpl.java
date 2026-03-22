package com.example.backend.order.service.Impl;

import com.example.backend.cart.dto.CartItemResponse;
import com.example.backend.cart.service.CartService;
import com.example.backend.order.entity.Order;
import com.example.backend.order.entity.OrderItem;
import com.example.backend.order.entity.OrderStatus;
import com.example.backend.order.exception.OrderCancellationException;
import com.example.backend.order.exception.OrderNotFoundException;
import com.example.backend.order.repository.OrderRepository;
import com.example.backend.order.service.OrderService;
import com.example.backend.product.entity.Product;
import com.example.backend.product.exception.ProductNotFoundException;
import com.example.backend.product.exception.ProductOutOfStockException;
import com.example.backend.product.repository.ProductRepository;
import com.example.backend.users.exception.UserNotFoundException;
import com.example.backend.users.entity.Users;
import com.example.backend.users.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final CartService cartService;

    private final UsersRepo usersRepo;

    @Override
    @Transactional
    public Order createOrderFromCart(String userEmail, String shippingAddress) {
        Users user = usersRepo.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("No User found with this email : " + userEmail)
        );
        List<CartItemResponse> cartItems = cartService.getCart(userEmail).getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .status(OrderStatus.CREATED)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemResponse ci : cartItems) {
            Product p = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with this id : " + ci.getProductId()));
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(subtotal);
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(ci.getQuantity())
                    .priceAtPurchase(p.getPrice())
                    .build();
            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        cartService.clearCart(userEmail);
        return saved;
    }

    @Override
    @Transactional
    public Order createDirectOrder(String userEmail, UUID productId, int quantity, String shippingAddress) {
        Users user = usersRepo.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("User not found with this email : " + userEmail)
        );
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + productId));
        if (product.getStock() < quantity) {
            throw new ProductOutOfStockException("Insufficient stock");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .status(OrderStatus.CREATED)
                .build();
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .build();
        order.getItems().add(item);
        order.setTotalAmount(
                product.getPrice().multiply(BigDecimal.valueOf(quantity))
        );
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("No order found with this Id : ", id));
        if (order.getUser() == null || !userEmail.equals(order.getUser().getEmail())) {
            throw new OrderNotFoundException("NO order found with this Id :", id);
        }

        return order;
    }

    @SuppressWarnings("NullableProblems")

    @Override
    public Page<Order> getOrdersForUser(String userEmail, Pageable pageable) {
        Users user = usersRepo.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("User not found with this email : " + userEmail)
        );
        return orderRepository.findAllByUser(user, pageable);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("No order found with this Id : ", orderId));
        if (!userEmail.equals(order.getUser().getEmail())) {
            throw new OrderNotFoundException("No order found with this Id : ", orderId);
        }

        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderCancellationException("Order cannot be cancelled at this stage");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
