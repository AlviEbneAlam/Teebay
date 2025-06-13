package com.shazam.teebay.repository;

import com.shazam.teebay.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, Long> {
}
