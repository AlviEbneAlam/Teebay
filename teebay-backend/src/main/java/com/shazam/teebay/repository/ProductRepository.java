package com.shazam.teebay.repository;

import com.shazam.teebay.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Long> {

    Page<Products> findAllByUserId(Long userId, Pageable pageable);

    Page<Products> findAllByAvailabilityStatusIn(List<String> statuses, Pageable pageable);
    Optional<Products> findByIdAndAvailabilityStatusIn(Long id, List<String> availabilityStatus);

}
