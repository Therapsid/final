package com.example.backend.seller.dto;

import com.example.backend.seller.entity.SellerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

@AllArgsConstructor
public class SellerRequestResponse {

    private Long id;

    private String userEmail;

    private String storeName;

    private String documentUrl;

    private String reason;

    private SellerRequestStatus status;

    private String createdAt;

    private String reviewedAt;

    private String reviewedBy;
}
