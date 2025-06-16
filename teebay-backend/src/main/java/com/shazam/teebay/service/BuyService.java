package com.shazam.teebay.service;

import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.entity.ProductPurchase;
import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.UserInfo;
import com.shazam.teebay.enums.ProductState;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BuyService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductPurchaseRepository purchaseRepository;

    public BuyService(ProductRepository productRepository,
                     UserRepository userRepository,
                       ProductPurchaseRepository purchaseRepository,
                       RentBookingsRepository rentBookingsRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository=userRepository;
        this.purchaseRepository=purchaseRepository;
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

}
