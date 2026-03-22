package com.example.backend.category.service.Impl;

import com.example.backend.common.dto.MessageResponse;
import com.example.backend.category.dto.CategoryRequest;
import com.example.backend.category.dto.CategoryResponse;
import com.example.backend.category.entity.Category;
import com.example.backend.category.exception.*;
import com.example.backend.category.mapper.CategoryMapper;
import com.example.backend.category.repository.CategoryRepository;
import com.example.backend.category.service.CategoryService;
import com.example.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new CategoryAlreadyExistsException("Category with this name already exists.");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));
            category.setParent(parent);
        }

        categoryRepository.save(category);
        return categoryMapper.toResponse(category, "Category created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        return categoryMapper.toResponse(category, "Category found successfully");
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());
        return rootCategories.stream()
                .map(c -> categoryMapper.toResponse(c, ""))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                throw new CategoryUpdateException("Category with this name already exists.");
            }

            category.setName(newName);
        }

        category.setDescription(request.getDescription());
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new InvalidCategoryException("Category cannot be its own parent.");
            }

            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));
            Category cur = newParent;
            while (cur != null) {
                if (cur.getId().equals(category.getId())) {
                    throw new InvalidCategoryException("Setting this parent would create a cycle.");
                }

                cur = cur.getParent();
            }

            category.setParent(newParent);
        } else {
            category.setParent(null);
        }

        categoryRepository.save(category);
        return categoryMapper.toResponse(category, "Category updated successfully");
    }

    @Override
    public MessageResponse deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        boolean hasSub = category.getSubCategories() != null && !category.getSubCategories().isEmpty();
        if (hasSub) {
            throw new CategoryDeletionException("Cannot delete category that has subcategories.");
        }

        if (productRepository.existsByCategory(category)) {
            throw new CategoryDeletionException("Cannot delete category that has products linked to it.");
        }

        categoryRepository.deleteById(id);
        return new MessageResponse("Category deleted successfully");
    }
}
