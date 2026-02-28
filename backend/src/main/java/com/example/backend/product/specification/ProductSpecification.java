package com.example.backend.product.specification;

import com.example.backend.product.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductSpecification implements Specification<Product> {

    private final ProductFilter filter;

    @Override
    public @Nullable Predicate toPredicate(Root<Product> root,
                                           CriteriaQuery<?> query,
                                           CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        UUID id = filter.getId();
        if (!ObjectUtils.isEmpty(id)) {
            predicates.add(criteriaBuilder.equal(root.get("id"), id));
        }

        String name = filter.getName();
        if (!ObjectUtils.isEmpty(name)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.
                    lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        String category = filter.getCategory();
        if (!ObjectUtils.isEmpty(category)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.
                    lower(root.get("category").get("name")), "%" + category.toLowerCase() + "%"));
        }

        BigDecimal price = filter.getPrice();
        if (!ObjectUtils.isEmpty(price)) {
            predicates.add(criteriaBuilder.equal(root.get("price"), price));
        }

        BigDecimal minPrice = filter.getMinPrice();
        if (!ObjectUtils.isEmpty(minPrice)) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        BigDecimal maxPrice = filter.getMaxPrice();
        if (!ObjectUtils.isEmpty(maxPrice)) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
