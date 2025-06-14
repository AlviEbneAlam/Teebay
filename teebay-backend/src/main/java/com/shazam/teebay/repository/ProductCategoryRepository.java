package com.shazam.teebay.repository;

import com.shazam.teebay.entity.ProductCategory;
import com.shazam.teebay.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductCategory pc WHERE pc.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    List<ProductCategory> findByProductId(Long productId);

}
