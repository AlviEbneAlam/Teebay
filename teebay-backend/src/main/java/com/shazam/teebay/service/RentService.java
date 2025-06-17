package com.shazam.teebay.service;

import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.Rent;
import com.shazam.teebay.entity.RentBookings;
import com.shazam.teebay.entity.UserInfo;
import com.shazam.teebay.enums.RentStatus;
import com.shazam.teebay.enums.TypeOfRent;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class RentService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final RentRepository rentRepository;
    private final UserRepository userRepository;
    private final ProductPurchaseRepository purchaseRepository;
    private final RentBookingsRepository rentBookingsRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RentService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductCategoryRepository productCategoryRepository,
                          RentRepository rentRepository, UserRepository userRepository,
                          ProductPurchaseRepository purchaseRepository,
                       RentBookingsRepository rentBookingsRepository
                       ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.rentRepository = rentRepository;
        this.userRepository=userRepository;
        this.purchaseRepository=purchaseRepository;
        this.rentBookingsRepository=rentBookingsRepository;
    }

    @Transactional
    public AddProductResponse bookForRent(Long productId, String rentStart, String rentEnd, int noOfHours) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = LocalDateTime.parse(rentStart, formatter);
        LocalDateTime endDate = LocalDateTime.parse(rentEnd, formatter);

        log.debug("Attempting to book product ID {} from {} to {} (noOfHours={})", productId, rentStart, rentEnd, noOfHours);

        if (endDate.isBefore(startDate) || startDate.isBefore(now)) {
            log.warn("Invalid rental period for product ID {}: start={}, end={}, now={}", productId, startDate, endDate, now);
            return new AddProductResponse("400", "Invalid rental period");
        }

        if (startDate.toLocalDate().equals(now.toLocalDate()) &&
                now.toLocalTime().isAfter(LocalTime.of(10, 0))) {
            log.warn("Same-day booking rejected after 10:00 AM for product ID {}", productId);
            return new AddProductResponse("400", "Same‑day rentals must be booked before 10:00 AM");
        }

        try {
            log.debug("Fetching product with ID {} and status AVAILABLE/RENTED", productId);
            Products product = productRepository
                    .findByIdAndAvailabilityStatusIn(productId, List.of("AVAILABLE", "RENTED"))
                    .orElseThrow(() -> new GraphQLValidationException("Product not available"));

            log.debug("Fetching rent info for rent ID {}", product.getRentId());
            Rent rent = rentRepository.findById(product.getRentId())
                    .orElseThrow(() -> new GraphQLValidationException("Rent info missing"));

            Duration duration = Duration.between(startDate, endDate);
            long units;
            if (rent.getTypeOfRent() == TypeOfRent.PER_HOUR) {
                units = duration.toHours() + (duration.toMinutesPart() > 0 ? 1 : 0);
            } else {
                units = duration.toDays() + (duration.toHoursPart() > 0 ? 1 : 0);
            }

            double total = units * rent.getRentPrice();
            log.debug("Calculated rent: units={} total={}", units, total);

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo renter = userRepository.findByEmail(email)
                    .orElseThrow(() -> new GraphQLValidationException("User not found"));

            RentBookings booking = new RentBookings();
            booking.setProductId(productId);
            booking.setRentId(rent.getId());
            booking.setRenterId(renter.getId());
            booking.setRentStartTime(startDate);
            booking.setRentEndTime(endDate);
            booking.setRentPeriod((int) units);
            booking.setRentTimeUnit(rent.getTypeOfRent() == TypeOfRent.PER_HOUR ? "hours" : "days");
            booking.setTotalRent(total);
            booking.setRentStatus(RentStatus.BOOKED);

            log.debug("Saving rent booking for product ID {} by user ID {}", productId, renter.getId());
            rentBookingsRepository.save(booking);

            product.setAvailabilityStatus("RENTED");
            productRepository.save(product);
            log.info("Successfully booked product ID {} for user {}", productId, email);

            return new AddProductResponse("200",
                    String.format("Booked %d %s; total rent: %.2f",
                            units,
                            rent.getTypeOfRent() == TypeOfRent.PER_HOUR ? "hour(s)" : "day(s)",
                            total));

        } catch (DataIntegrityViolationException ex) {
            log.warn("Conflict while booking product ID {}: {}", productId, ex.getLocalizedMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new AddProductResponse("400", "Product already booked for this period");
        } catch (GraphQLValidationException ex) {
            log.warn("Validation failed while booking product ID {}: {}", productId, ex.getMessage());
            return new AddProductResponse("400", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while booking product ID {}", productId, ex);
            return new AddProductResponse("500", "Internal server error");
        }
    }

}
