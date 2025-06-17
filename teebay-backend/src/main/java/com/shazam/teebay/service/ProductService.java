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
import java.util.Collections;
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
        log.info("Attempting to add product for request: {}", request);
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log.debug("Authenticated user email: {}", email);

            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new GraphQLValidationException("User not found: " + email));

            Products product = new Products();
            populateProductFromRequest(product, request, user.getId());
            Products savedProduct = productRepository.save(product);
            log.info("Product saved with ID: {}", savedProduct.getId());

            for (String name : request.categoriesList()) {
                Category category = categoryRepository.findByName(name)
                        .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name));
                ProductCategory mapping = new ProductCategory();
                mapping.setProductId(savedProduct.getId());
                mapping.setCategoryId(category.getId());
                productCategoryRepository.save(mapping);
                log.debug("Mapped category '{}' to product ID {} in add product", name, savedProduct.getId());
            }

            return new AddProductResponse("200", "Product added successfully with ID: " + savedProduct.getId());
        } catch (GraphQLValidationException e) {
            log.warn("Validation failed while adding product: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while adding product", e);
            throw new GraphQLDataProcessingException("Failed to add product", e);
        }
    }

    @Transactional
    public AddProductResponse editProduct(Long productId, AddProductRequest request) {
        log.info("Attempting to edit product with ID: {}", productId);
        try {
            Products existing = productRepository.findById(productId)
                    .orElseThrow(() -> new GraphQLValidationException("Product not found with ID: " + productId));

            populateProductFromRequest(existing, request, existing.getUserId());
            Products updatedProduct = productRepository.save(existing);
            log.info("Product updated with ID: {}", updatedProduct.getId());

            productCategoryRepository.deleteByProductId(productId);
            log.debug("Deleted existing category mappings for product ID: {}", productId);

            for (String name : request.categoriesList()) {
                Category category = categoryRepository.findByName(name)
                        .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name));
                ProductCategory mapping = new ProductCategory();
                mapping.setProductId(productId);
                mapping.setCategoryId(category.getId());
                productCategoryRepository.save(mapping);
                log.debug("Mapped category '{}' to product ID {} in edit product", name, productId);
            }

            return new AddProductResponse("200", "Product updated successfully with ID: " + updatedProduct.getId());
        } catch (GraphQLValidationException e) {
            log.warn("Validation failed while editing product ID {}: {}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while editing product ID {}", productId, e);
            throw new GraphQLDataProcessingException("Failed to update product", e);
        }
    }

    private void populateProductFromRequest(Products product, AddProductRequest request, Long userId) {
        log.debug("Populating product object from request: {}", request);
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setSellingPrice(request.sellingPrice());
        product.setAvailabilityStatus("AVAILABLE");
        product.setUserId(userId);

        if (product.getId() != null) {
            productCategoryRepository.deleteByProductId(product.getId());
            log.debug("Deleted category mappings for product ID during update: {}", product.getId());
        }

        if (request.rent() > 0) {
            Rent rent = product.getRentId() != null
                    ? rentRepository.findById(product.getRentId()).orElse(new Rent())
                    : new Rent();

            rent.setRentPrice(request.rent());
            rent.setTypeOfRent(parseTypeOfRent(request.typeOfRent()));
            Rent savedRent = rentRepository.save(rent);
            product.setRentId(savedRent.getId());
            log.debug("Saved rent info for product ID: {} with rent ID: {}", product.getId(), savedRent.getId());
        } else {
            product.setRentId(null);
            log.debug("Cleared rent info for product ID: {}", product.getId());
        }
    }

    private TypeOfRent parseTypeOfRent(String value) {
        try {
            return TypeOfRent.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid typeOfRent received: {}", value);
            throw new GraphQLValidationException("Invalid typeOfRent value: " + value);
        }
    }

    @Transactional
    public ProductPageDto getProductsByUserPaginated(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching paginated products for user: {}", email);

        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GraphQLValidationException("Authenticated user not found: " + email));

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Products> productPage = productRepository.findAllByUserId(user.getId(), pageable);
            List<ProductDto> productDTOs = mapToDtoList(productPage.getContent());
            log.info("Fetched {} products for user ID: {}", productDTOs.size(), user.getId());

            return new ProductPageDto(
                    productDTOs,
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getNumber()
            );
        } catch (Exception ex) {
            log.error("Error while fetching products for user ID {}: {}", user.getId(), ex.getMessage(), ex);
            throw new GraphQLDataProcessingException("Could not fetch user products", ex);
        }
    }


    @Transactional(readOnly = true)
    public ProductPageDto getAllProductsByStatus(int page, int size) {
        log.info("Fetching all products by status: AVAILABLE and RENTED, page: {}, size: {}", page, size);
        List<String> statuses = List.of(ProductState.AVAILABLE.name(), ProductState.RENTED.name());

        Page<Products> productPage;
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            productPage = productRepository.findAllByAvailabilityStatusIn(statuses, pageable);
            log.info("Fetched {} products by status", productPage.getContent().size());
        } catch (Exception ex) {
            log.error("Exception while fetching available and rented products: {}", ex.getLocalizedMessage(), ex);
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
        log.info("Attempting to soft-delete product with ID: {}", productId);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email in delete product: {}", email);
                    return new GraphQLValidationException("User not found: " + email);
                });

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new GraphQLValidationException("Product not found with ID: " + productId);
                });

        if (!product.getUserId().equals(user.getId())) {
            log.warn("User {} is not authorized to delete product ID: {}", email, productId);
            
            return new AddProductResponse("400", "You are not authorized to delete this product.");
        }

        // 🔍 Check for ongoing bookings
        List<RentBookings> bookings = rentBookingsRepository
                .findByProductIdOrderByRentEndTimeDesc(productId);

        if (!bookings.isEmpty()) {
            RentBookings latestBooking = bookings.get(0);
            if (latestBooking.getRentEndTime().isAfter(LocalDateTime.now())) {
                log.warn("Product ID {} has ongoing booking until {}", productId, latestBooking.getRentEndTime());
                return new AddProductResponse("400", "Cannot delete product with ongoing bookings");
            }
        }

        try {
            product.setAvailabilityStatus(ProductState.DELETED.name());
            productRepository.save(product);
            log.info("Soft-deleted product by setting availability status to DELETED for ID: {}", productId);

            return new AddProductResponse("200", "Product soft-deleted (status set to DELETED) for ID: " + productId);
        } catch (Exception e) {
            log.error("Failed to soft-delete product with ID {}: {}", productId, e.getLocalizedMessage(), e);
            return new AddProductResponse("400", "Cannot delete the product");
        }
    }



    private List<ProductDto> mapToDtoList(List<Products> products) {
        log.debug("Mapping list of {} products to DTOs", products.size());
        return products.stream()
                .map(product -> mapToProductDto(product, false))
                .toList();
    }

    private ProductDto mapToProductDto(Products product, boolean includeBookingInfo) {
        log.debug("Mapping product ID: {} to DTO. includeBookingInfo: {}", product.getId(), includeBookingInfo);

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

                if (booking.getRentEndTime().isAfter(LocalDateTime.now())) {
                    rentStartTime = booking.getRentStartTime();
                    rentEndTime = booking.getRentEndTime();
                } else {
                    log.info("Rental period is over for product ID: {}. Updating status to AVAILABLE.", product.getId());
                    availabilityStatus = "AVAILABLE";
                    product.setAvailabilityStatus("AVAILABLE");
                    productRepository.save(product);
                }
            } else {
                log.info("No valid booking found for product ID: {}. Updating status to AVAILABLE.", product.getId());
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

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        log.info("Fetching product by ID: {}", productId);
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("User not found for email: {}", email);
                        return new GraphQLValidationException("User not found: " + email);
                    });

            Products product = productRepository.findByIdAndUserId(productId, user.getId())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {} for user: {}", productId, email);
                        return new GraphQLValidationException("Product not found with ID: " + productId + " for user: " + email);
                    });

            return mapToProductDto(product, true);
        } catch (GraphQLValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product by ID {}: {}", productId, e.getLocalizedMessage(), e);
            throw new GraphQLDataProcessingException("Failed to fetch product by ID", e);
        }
    }

    @Transactional(readOnly = true)
    public ProductPageDto getProductsByUserAndStatus(String status, int page, int size) {
        log.info("Fetching products by user and status: {}, page: {}, size: {}", status, page, size);
        String email = "";
        if (!status.equals("SOLD") && !status.equals("RENTED") && !status.equals("AVAILABLE")) {
            log.warn("Invalid product status requested: {}", status);
            throw new GraphQLValidationException("Invalid product status: " + status);
        }

        try {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
            String finalEmail = email;
            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authenticated user not found: {}", finalEmail);
                        return new GraphQLValidationException("Authenticated user not found: " + finalEmail);
                    });

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Products> productPage = productRepository.findAllByUserIdAndAvailabilityStatus(user.getId(), status, pageable);
            log.info("Found {} products with status {}", productPage.getContent().size(), status);

            List<ProductDto> productDTOs = productPage.getContent().stream()
                    .map(product -> mapToProductDto(product, true))
                    .toList();

            return new ProductPageDto(
                    productDTOs,
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getNumber()
            );
        } catch (Exception ex) {
            log.error("Failed to fetch products for user {} with status {}: {}", email, status, ex.getLocalizedMessage(), ex);
            return new ProductPageDto(
                    new ArrayList<>(),
                    0,
                    0,
                    0
            );
        }
    }

    @Transactional(readOnly = true)
    public ProductPageDto getBoughtProductsByUser(int page, int size) {
        log.info("Fetching bought products, page: {}, size: {}", page, size);
        String email = "";
        try {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
            String finalEmail = email;
            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authenticated user not found for email in get bought product: {}", finalEmail);
                        return new GraphQLValidationException("Authenticated user not found for email: " + finalEmail);
                    });

            List<Long> productIds = purchaseRepository.findProductIdsByBuyerId(user.getId());
            log.info("User {} has bought {} products", email, productIds.size());

            if (productIds.isEmpty()) {
                return new ProductPageDto(
                        new ArrayList<>(),
                        0,
                        0,
                        0
                );
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Products> productPage = productRepository.findAllByIdIn(productIds, pageable);

            List<ProductDto> productDTOs = productPage.getContent().stream()
                    .map(product -> mapToProductDto(product, true))
                    .toList();

            return new ProductPageDto(
                    productDTOs,
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getNumber()
            );
        } catch (Exception ex) {
            log.error("Failed to fetch bought products for user {}: {}", email, ex.getLocalizedMessage(), ex);
            return new ProductPageDto(
                    new ArrayList<>(),
                    0,
                    0,
                    0
            );
        }
    }

    @Transactional(readOnly = true)
    public ProductPageDto getBorrowedProductsByUser(int page, int size) {
        log.info("Fetching borrowed products, page: {}, size: {}", page, size);
        String email = "";
        try {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
            String finalEmail = email;
            UserInfo user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authenticated user not found for email: {}", finalEmail);
                        return new GraphQLValidationException("Authenticated user not found for email: " + finalEmail);
                    });

            List<Long> productIds = rentBookingsRepository.findDistinctProductIdsByRenterId(user.getId());
            log.info("User {} has borrowed {} products", email, productIds.size());

            if (productIds.isEmpty()) {
                return new ProductPageDto(
                        new ArrayList<>(),
                        0,
                        0,
                        0
                );
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Products> productPage = productRepository.findAllByIdIn(productIds, pageable);

            List<ProductDto> productDTOs = productPage.getContent().stream()
                    .map(product -> mapToProductDto(product, true))
                    .toList();

            return new ProductPageDto(
                    productDTOs,
                    productPage.getTotalPages(),
                    productPage.getTotalElements(),
                    productPage.getNumber()
            );
        } catch (Exception ex) {
            log.error("Failed to fetch borrowed products for user {}: {}", email, ex.getLocalizedMessage(), ex);
            return new ProductPageDto(
                    new ArrayList<>(),
                    0,
                    0,
                    0
            );
        }
    }



}
