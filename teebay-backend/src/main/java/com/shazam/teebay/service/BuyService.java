package com.shazam.teebay.service;

import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.entity.ProductPurchase;
import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.RentBookings;
import com.shazam.teebay.entity.UserInfo;
import com.shazam.teebay.enums.ProductState;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class BuyService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductPurchaseRepository purchaseRepository;
    private final RentBookingsRepository rentBookingsRepository;

    public BuyService(ProductRepository productRepository,
                     UserRepository userRepository,
                       ProductPurchaseRepository purchaseRepository,
                       RentBookingsRepository rentBookingsRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository=userRepository;
        this.purchaseRepository=purchaseRepository;
        this.rentBookingsRepository=rentBookingsRepository;
    }

    @Transactional
    public AddProductResponse buyProduct(Long productId, String status) {

        log.debug("Attempting to purchase product ID {} with status '{}'", productId, status);

        try {
            ProductState.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid availability status '{}' for product ID {}", status, productId);
            throw new GraphQLValidationException("Invalid availability status: " + status);
        }

        try {
            log.debug("Fetching product with ID {}", productId);
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new GraphQLValidationException("Product not found"));

            boolean alreadySold = purchaseRepository.existsByProductId(productId);
            if (alreadySold) {
                log.warn("Product ID {} is already sold", productId);
                return new AddProductResponse("400", "Product has already been sold.");
            }

            Optional<RentBookings> latestBookingOpt = rentBookingsRepository
                    .findTopByProductIdOrderByRentStartTimeDesc(product.getId());

            if (latestBookingOpt.isPresent()) {
                RentBookings booking = latestBookingOpt.get();
                if (booking.getRentEndTime().isAfter(LocalDateTime.now())) {
                    log.warn("Product ID {} has an ongoing rent booking", productId);
                    return new AddProductResponse("400", "Product has an ongoing rent booking.");
                }
            }

            product.setAvailabilityStatus(status);
            productRepository.save(product);
            log.info("Updated availability status of product ID {} to {}", productId, status);

            if ("SOLD".equalsIgnoreCase(status)) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                UserInfo buyer = userRepository.findByEmail(email)
                        .orElseThrow(() -> new GraphQLValidationException("Authenticated user not found: " + email));

                ProductPurchase purchase = new ProductPurchase();
                purchase.setProductId(productId);
                purchase.setBuyerId(buyer.getId());

                try {
                    purchaseRepository.save(purchase);
                    log.info("Recorded purchase of product ID {} by user {}", productId, email);
                } catch (DataIntegrityViolationException e) {
                    log.warn("Duplicate purchase attempt for product ID {}", productId);
                    return new AddProductResponse("400", "Product has already been sold .");
                }
            }

            return new AddProductResponse("200", "Product has been successfully purchased");

        } catch (GraphQLValidationException ex) {
            log.warn("Validation error during purchase of product ID {}: {}", productId, ex.getMessage());
            return new AddProductResponse("400", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while changing product status for product ID {}: {}", productId, ex.getMessage(), ex);
            return new AddProductResponse("500", "Internal server error");
        }
    }

}
