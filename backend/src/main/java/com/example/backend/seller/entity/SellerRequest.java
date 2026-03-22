package com.example.backend.seller.entity;

import com.example.backend.users.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class SellerRequest {
    @Id
    @GeneratedValue

    private Long id;

    @OneToOne(optional = false)
    private Users user;

    private String storeName;

    private String documentUrl;

    @Column(length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    private SellerRequestStatus status = SellerRequestStatus.PENDING;

    @CreationTimestamp

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    private String reviewedBy;
}
