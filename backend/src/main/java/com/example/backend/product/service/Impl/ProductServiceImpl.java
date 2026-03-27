package com.example.backend.product.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend.category.entity.Category;
import com.example.backend.category.exception.CategoryNotFoundException;
import com.example.backend.category.repository.CategoryRepository;
import com.example.backend.product.dto.ProductRequest;
import com.example.backend.product.dto.ProductResponse;
import com.example.backend.product.entity.Product;
import com.example.backend.product.exception.*;
import com.example.backend.product.mapper.ProductMapper;
import com.example.backend.product.repository.ProductRepository;
import com.example.backend.product.service.ProductService;
import com.example.backend.common.dto.MessageResponse;
import com.example.backend.users.entity.Users;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.product.specification.ProductFilter;
import com.example.backend.product.specification.ProductSpecification;
import com.example.backend.users.repository.UsersRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final CategoryRepository categoryRepo;
    private final Cloudinary cloudinary;
    private final UsersRepo usersRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(ProductRequest request, MultipartFile file, String sellerEmail) throws IOException {
        String name = request.getName().trim();
        if (repo.existsByNameIgnoreCase(name)) {
            throw new ProductAlreadyExistsException("Product with this name already exists.");
        }

        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new InvalidProductException("Category ID is required");
        }

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + categoryId));
        Users seller = usersRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", sellerEmail));

        Product product = Product.builder()
                .name(name)
                .description(request.getDescription())
                .price(BigDecimal.valueOf(request.getPrice()))
                .stock(request.getStock())
                .category(category)
                .seller(seller)
                .active(true)
                .build();

        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Products",
                            "public_id", name,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            product.setImageUrl(imageUrl);
        }

        repo.save(product);
        return productMapper.toResponse(product, "Product created successfully :<>:");
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ProductInactiveException("Product is not active right now.");
        }

        if (product.getStock() == null || product.getStock() <= 0) {
            throw new ProductOutOfStockException("Sorry, product is out of stock.");
        }

        return productMapper.toResponse(product, "Product found successfully :D ");
    }

    @Override
    public List<ProductResponse> getProductsByFilter(ProductFilter filter) {
        List<Product> products = repo.findAll(new ProductSpecification(filter));
        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found matching the given criteria.");
        }

        return products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getActive()))
                .filter(product -> product.getStock() != null && product.getStock() > 0)
                .map(product -> productMapper.toResponse(product, "Product fetched successfully"))
                .toList();
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = repo.findAll(pageable);
        if (products.isEmpty()) {
            return Page.empty(pageable);
        }

        return products.map(p -> productMapper.toResponse(p, "Products found successfully :D "));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request, MultipartFile file, String sellerEmail) throws IOException {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not the owner of this product");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!product.getName().equalsIgnoreCase(newName) &&
                    repo.existsByNameIgnoreCase(newName)) {
                throw new ProductAlreadyExistsException("Product with this name already exists.");
            }

            product.setName(newName);
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            product.setDescription(request.getDescription().trim());
        }

        if (request.getPrice() != null) {
            if (request.getPrice() < 0) {
                throw new InvalidProductException("Price cannot be negative");
            }

            product.setPrice(BigDecimal.valueOf(request.getPrice()));
        }

        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new InvalidProductException("Stock cannot be negative");
            }

            product.setStock(request.getStock());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found with id : " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "Products",
                            "public_id", product.getName(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            product.setImageUrl(imageUrl);
        }

        repo.save(product);
        return productMapper.toResponse(product, "Product updated successfully");
    }

    @Override
    public MessageResponse deleteProduct(UUID id, String sellerEmail) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not allowed to delete this product");
        }

        repo.deleteById(id);
        return new MessageResponse("Product deleted successfully");
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
