package com.shazam.teebay.service;

import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.entity.Category;
import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.Rent;
import com.shazam.teebay.enums.ListedFor;
import com.shazam.teebay.enums.TypeOfRent;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.CategoryRepository;
import com.shazam.teebay.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public AddProductResponse addProduct(AddProductRequest request) {
        try {
            Products product = new Products();
            populateProductFromRequest(product, request);
            Products saved = productRepository.save(product);
            return new AddProductResponse("200", "Product added successfully with ID: " + saved.getId());
        } catch (GraphQLValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to add product", e);
        }
    }

    @Transactional
    public AddProductResponse editProduct(Long productId, AddProductRequest request) {
        try {
            Products existing = productRepository.findById(productId)
                    .orElseThrow(() -> new GraphQLValidationException("Product not found with ID: " + productId));
            populateProductFromRequest(existing, request);
            Products updated = productRepository.save(existing);
            return new AddProductResponse("200", "Product updated successfully with ID: " + updated.getId());
        } catch (GraphQLValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to update product", e);
        }
    }

    private void populateProductFromRequest(Products product, AddProductRequest request) {
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setListedFor(parseListedFor(request.listedFor()));
        product.setSellingPrice(request.sellingPrice());
        product.setAvailabilityStatus("AVAILABLE");

        Set<Category> categories = request.categoriesList().stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name)))
                .collect(Collectors.toSet());
        product.setCategories(categories);

        if (!request.typeOfRent().isBlank() && request.rent() > 0) {
            Rent rent = product.getRent() != null ? product.getRent() : new Rent();
            rent.setRentPrice(request.rent());
            rent.setTypeOfRent(parseTypeOfRent(request.typeOfRent()));
            rent.setRentBufferPeriod(request.rentBufferPeriod());
            product.setRent(rent);
        } else {
            product.setRent(null);
        }
    }

    private ListedFor parseListedFor(String value) {
        try {
            return ListedFor.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid listedFor value: " + value);
        }
    }

    private TypeOfRent parseTypeOfRent(String value) {
        try {
            return TypeOfRent.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid typeOfRent value: " + value);
        }
    }
}