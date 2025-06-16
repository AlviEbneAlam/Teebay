package com.shazam.teebay.service;

import com.shazam.teebay.Utils.DateUtil;
import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.dto.ProductDto;
import com.shazam.teebay.dto.ProductPageDto;
import com.shazam.teebay.entity.*;
import com.shazam.teebay.enums.ProductState;
import com.shazam.teebay.enums.TypeOfRent;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ProductPurchaseRepository purchaseRepository;
    private final RentBookingsRepository rentBookingsRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductCategoryRepository productCategoryRepository,
                          RentRepository rentRepository, UserRepository userRepository,
                          ProductPurchaseRepository purchaseRepository,
                          RentBookingsRepository rentBookingsRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.rentRepository = rentRepository;
        this.userRepository=userRepository;
        this.purchaseRepository=purchaseRepository;
        this.rentBookingsRepository=rentBookingsRepository;
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

        Page<Products> productPage;
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            productPage = productRepository.findAllByUserId(user.getId(), pageable);
        } catch (Exception ex) {
            log.info("Exception while fetching product by user id: {}", ex.getLocalizedMessage());
            throw new GraphQLDataProcessingException("Could not fetch user products", ex);
        }

        List<ProductDto> productDTOs = mapToDtoList(productPage.getContent());

        return new ProductPageDto(
                productDTOs,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getNumber()
        );
    }

    @Transactional(readOnly = true)
    public ProductPageDto getAllProductsByStatus(int page, int size) {

        List<String> statuses = List.of(ProductState.AVAILABLE.name(), ProductState.RENTED.name());

        Page<Products> productPage;
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            productPage = productRepository.findAllByAvailabilityStatusIn(statuses, pageable);
        } catch (Exception ex) {
            log.error("Exception while fetching available and rented products: {}", ex.getLocalizedMessage());
            throw new GraphQLDataProcessingException("Could not fetch products", ex);
        }

        List<ProductDto> productDTOs = productPage.getContent().stream()
                .map(product -> mapToProductDto(product, true))
                .toList();

        return new ProductPageDto(
                productDTOs,
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getNumber()
        );
    }

    @Transactional
    public AddProductResponse deleteProduct(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GraphQLValidationException("User not found: " + email));

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new GraphQLValidationException("Product not found with ID: " + productId));

        if (!product.getUserId().equals(user.getId())) {
            throw new GraphQLValidationException("You are not authorized to delete this product.");
        }

        try {
            // Delete category mappings
            productCategoryRepository.deleteByProductId(productId);

            // Delete rent if exists
            if (product.getRentId() != null) {
                rentRepository.deleteById(product.getRentId());
            }

            // Delete the product
            productRepository.deleteById(productId);

            return new AddProductResponse("200", "Product deleted successfully with ID: " + productId);
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to delete product", e);
        }
    }

    private List<ProductDto> mapToDtoList(List<Products> products) {
        return products.stream()
                .map(product -> mapToProductDto(product, false)) // no booking info needed
                .toList();
    }

    @Transactional
    public AddProductResponse buyProduct(Long productId, String status) {

        try {
            ProductState.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid availability status: " + status);
        }

        try {
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new GraphQLValidationException("Product not found"));

            boolean alreadySold = purchaseRepository.existsByProductId(productId);
            if (alreadySold) {
                return new AddProductResponse("400", "Product has already been sold.");
            }

            // Update product status
            product.setAvailabilityStatus(status);
            productRepository.save(product);

            if ("SOLD".equalsIgnoreCase(status)) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                UserInfo buyer = userRepository.findByEmail(email)
                        .orElseThrow(() -> new GraphQLValidationException("Authenticated user not found: " + email));

                ProductPurchase purchase = new ProductPurchase();
                purchase.setProductId(productId);
                purchase.setBuyerId(buyer.getId());

                // Save purchase and catch unique constraint violation
                try {
                    purchaseRepository.save(purchase);
                } catch (DataIntegrityViolationException e) {
                    // This exception happens if unique constraint on product_id is violated
                    return new AddProductResponse("400", "Product has already been sold (race condition prevented).");
                }
            }

            return new AddProductResponse("200", "Product marked as " + status);

        } catch (GraphQLValidationException ex) {
            return new AddProductResponse("400", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while changing product status: {}", ex.getMessage(), ex);
            return new AddProductResponse("500", "Internal server error");
        }
    }



    private ProductDto mapToProductDto(Products product, boolean includeBookingInfo) {
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

        LocalDateTime rentStartTime = null;
        LocalDateTime rentEndTime = null;
        String availabilityStatus = product.getAvailabilityStatus();

        if (includeBookingInfo && "RENTED".equalsIgnoreCase(availabilityStatus)) {
            Optional<RentBookings> latestBookingOpt = rentBookingsRepository
                    .findTopByProductIdOrderByRentStartTimeDesc(product.getId());

            if (latestBookingOpt.isPresent()) {
                RentBookings booking = latestBookingOpt.get();

                // Check if rentEndTime is after now
                if (booking.getRentEndTime().isAfter(LocalDateTime.now())) {
                    // Still rented: keep start/end times
                    rentStartTime = booking.getRentStartTime();
                    rentEndTime = booking.getRentEndTime();
                } else {
                    // Rental period is over: update status to AVAILABLE
                    availabilityStatus = "AVAILABLE";

                    // Optionally update the product entity in DB immediately
                    product.setAvailabilityStatus("AVAILABLE");
                    productRepository.save(product);
                }
            } else {
                // No bookings found: also mark available
                availabilityStatus = "AVAILABLE";
                product.setAvailabilityStatus("AVAILABLE");
                productRepository.save(product);
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
                availabilityStatus,
                DateUtil.formatLocalDateTime(product.getCreatedAt()),
                rentStartTime,
                rentEndTime
        );
    }

}
