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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    public AddProductResponse bookForRent(Long productId, LocalDateTime rentStart, LocalDateTime rentEnd) {
        LocalDateTime now = LocalDateTime.now();

        if (rentEnd.isBefore(rentStart) || rentStart.isBefore(now)) {
            return new AddProductResponse("400", "Invalid rental period");
        }

        //Same day booking
        if (rentStart.toLocalDate().equals(now.toLocalDate()) &&
                now.toLocalTime().isAfter(LocalTime.of(10, 0))) {
            return new AddProductResponse("400",
                    "Same‑day rentals must be booked before 10:00 AM");
        }

        try {

            Products product = productRepository
                    .findByIdAndAvailabilityStatus(productId, "AVAILABLE")
                    .orElseThrow(() -> new GraphQLValidationException("Product not available"));

            Rent rent = rentRepository.findById(product.getRentId())
                    .orElseThrow(() -> new GraphQLValidationException("Rent info missing"));


            Duration duration = Duration.between(rentStart, rentEnd);
            long units;
            if (rent.getTypeOfRent() == TypeOfRent.PER_HOUR) {
                units = duration.toHours() + (duration.toMinutesPart() > 0 ? 1 : 0);
            } else {
                units = duration.toDays() + (duration.toHoursPart() > 0 ? 1 : 0);
            }
            double total = units * rent.getRentPrice();


            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo renter = userRepository.findByEmail(email)
                    .orElseThrow(() -> new GraphQLValidationException("User not found"));


            RentBookings booking = new RentBookings();
            booking.setProductId(productId);
            booking.setRentId(rent.getId());
            booking.setRenterId(renter.getId());
            booking.setRentStartTime(rentStart);
            booking.setRentEndTime(rentEnd);
            booking.setRentPeriod((int) units);
            booking.setRentTimeUnit(rent.getTypeOfRent() == TypeOfRent.PER_HOUR ? "hours" : "days");
            booking.setTotalRent(total);
            booking.setRentStatus(RentStatus.BOOKED);
            rentBookingsRepository.save(booking);


            product.setAvailabilityStatus("RENTED");
            productRepository.save(product);

            return new AddProductResponse("200",
                    String.format("Booked %d %s; total rent: %.2f",
                            units,
                            rent.getTypeOfRent() == TypeOfRent.PER_HOUR ? "hour(s)" : "day(s)",
                            total));

        } catch (GraphQLValidationException ex) {
            return new AddProductResponse("400", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error booking rental", ex);
            return new AddProductResponse("500", "Internal server error");
        }
    }
}
