package com.example.backend.order.repository;


import com.example.backend.order.entity.Order;
import com.example.backend.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUser(Users user, Pageable pageable);
    Optional<Order> findById(Long id);
}