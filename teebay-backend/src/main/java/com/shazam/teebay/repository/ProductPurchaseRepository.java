package com.shazam.teebay.repository;

import com.shazam.teebay.entity.ProductPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductPurchaseRepository extends JpaRepository<ProductPurchase, Long> {

    boolean existsByProductId(Long productId);

    @Query("SELECT p.productId FROM ProductPurchase p WHERE p.buyerId = :buyerId")
    List<Long> findProductIdsByBuyerId(@Param("buyerId") Long buyerId);
}
