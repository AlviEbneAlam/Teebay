package com.shazam.teebay.service;

import com.shazam.teebay.Utils.DateUtil;
import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.dto.ProductDto;
import com.shazam.teebay.dto.ProductPageDto;
import com.shazam.teebay.entity.*;
import com.shazam.teebay.enums.TypeOfRent;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final RentRepository rentRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductCategoryRepository productCategoryRepository,
                          RentRepository rentRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.rentRepository = rentRepository;
        this.userRepository=userRepository;
    }

    @Transactional
    public AddProductResponse addProduct(AddProductRequest request) {
        try {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new GraphQLValidationException("User not found: " + email));

            Products product = new Products();
            populateProductFromRequest(product, request,user.getId());
            Products savedProduct = productRepository.save(product);

            // Save product-category mappings
            for (String name : request.categoriesList()) {
                Category category = categoryRepository.findByName(name)
                        .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name));
                ProductCategory mapping = new ProductCategory();
                mapping.setProductId(savedProduct.getId());
                mapping.setCategoryId(category.getId());
                productCategoryRepository.save(mapping);
            }

            return new AddProductResponse("200", "Product added successfully with ID: " + savedProduct.getId());
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

            populateProductFromRequest(existing, request, existing.getUserId());
            Products updatedProduct = productRepository.save(existing);

            // Delete old mappings
            productCategoryRepository.deleteByProductId(productId);

            // Save new mappings
            for (String name : request.categoriesList()) {
                Category category = categoryRepository.findByName(name)
                        .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name));
                ProductCategory mapping = new ProductCategory();
                mapping.setProductId(productId);
                mapping.setCategoryId(category.getId());
                productCategoryRepository.save(mapping);
            }

            return new AddProductResponse("200", "Product updated successfully with ID: " + updatedProduct.getId());
        } catch (GraphQLValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to update product", e);
        }
    }

    private void populateProductFromRequest(Products product, AddProductRequest request, Long userId) {
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setSellingPrice(request.sellingPrice());
        product.setAvailabilityStatus("AVAILABLE");
        product.setUserId(userId);

        if (product.getId() != null) {
            productCategoryRepository.deleteByProductId(product.getId());
        }

        if ( request.rent() > 0) {
            Rent rent = product.getRentId() != null
                    ? rentRepository.findById(product.getRentId()).orElse(new Rent())
                    : new Rent();

            rent.setRentPrice(request.rent());
            rent.setTypeOfRent(parseTypeOfRent(request.typeOfRent()));
            Rent savedRent = rentRepository.save(rent);
            product.setRentId(savedRent.getId());
        } else {
            product.setRentId(null);
        }
    }


    private TypeOfRent parseTypeOfRent(String value) {
        try {
            return TypeOfRent.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid typeOfRent value: " + value);
        }
    }

    @Transactional
    public ProductPageDto getProductsByUserPaginated(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GraphQLValidationException("Authenticated user not found: " + email));

        Page<Products> productPage=null;
        try{
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            productPage = productRepository.findAllByUserId(user.getId(), pageable);
        }
        catch(Exception ex){
            log.info("Exception while fetching product by user id: {}",ex.getLocalizedMessage());
        }

        List<ProductDto> productDTOs=new ArrayList<>();
        if(productPage!=null){
            productDTOs = productPage.getContent().stream().map(product -> {
                List<Long> categoryIds = productCategoryRepository.findByProductId(product.getId()).stream()
                        .map(ProductCategory::getCategoryId)
                        .toList();

                List<String> categoryNames = categoryIds.stream()
                        .map(categoryRepository::findById)
                        .filter(Optional::isPresent)
                        .map(opt -> opt.get().getName())
                        .toList();

                Double rentPrice = null;
                String typeOfRent = null;
                if (product.getRentId() != null) {
                    Rent rent = rentRepository.findById(product.getRentId()).orElse(null);
                    if (rent != null) {
                        rentPrice = rent.getRentPrice();
                        typeOfRent = rent.getTypeOfRent().toString();
                    }
                }

                return new ProductDto(
                        product.getId(),
                        product.getTitle(),
                        product.getDescription(),
                        categoryNames,
                        product.getSellingPrice(),
                        rentPrice,
                        typeOfRent,
                        product.getAvailabilityStatus(),
                        DateUtil.formatLocalDateTime(product.getCreatedAt())
                );
            }).toList();

        }

        return new ProductPageDto(
                productDTOs,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getNumber()
        );
    }
}
