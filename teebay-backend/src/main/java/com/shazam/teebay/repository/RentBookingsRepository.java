package com.shazam.teebay.repository;

import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.RentBookings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentBookingsRepository extends JpaRepository<RentBookings, Long> {

    Optional<RentBookings> findTopByProductIdOrderByRentStartTimeDesc(Long productId);

}
