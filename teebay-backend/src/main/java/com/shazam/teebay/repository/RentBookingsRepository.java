package com.shazam.teebay.repository;

import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.RentBookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentBookingsRepository extends JpaRepository<RentBookings, Long> {

    Optional<RentBookings> findTopByProductIdOrderByRentStartTimeDesc(Long productId);

    @Query("SELECT DISTINCT rb.productId FROM RentBookings rb WHERE rb.renterId = :renterId")
    List<Long> findDistinctProductIdsByRenterId(@Param("renterId") Long renterId);

    List<RentBookings> findByProductIdOrderByRentEndTimeDesc(Long productId);


}
