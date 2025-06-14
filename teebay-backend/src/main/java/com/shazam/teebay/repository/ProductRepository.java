package com.shazam.teebay.repository;

import com.shazam.teebay.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, Long> {

    Page<Products> findAllByUserId(Long userId, Pageable pageable);
}
