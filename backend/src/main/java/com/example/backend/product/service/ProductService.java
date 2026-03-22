package com.example.backend.product.service;

import com.example.backend.product.dto.ProductRequest;
import com.example.backend.product.dto.ProductResponse;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.product.specification.ProductFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public interface ProductService {
    ProductResponse createProduct(ProductRequest request, MultipartFile file, String sellerEmail) throws IOException;

    ProductResponse getProductById(UUID id);

    List<ProductResponse> getProductsByFilter(ProductFilter filter);

    org.springframework.data.domain.Page<ProductResponse> getAllProducts(int page, int size);

    ProductResponse updateProduct(UUID id, ProductRequest request, MultipartFile file, String sellerEmail) throws IOException;

    MessageResponse deleteProduct(UUID id, String sellerEmail);
}
