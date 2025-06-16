package com.shazam.teebay.repository;

import com.shazam.teebay.entity.ProductPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPurchaseRepository extends JpaRepository<ProductPurchase, Long> {

    boolean existsByProductId(Long productId);
}
