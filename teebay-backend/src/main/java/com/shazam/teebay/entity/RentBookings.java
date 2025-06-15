package com.shazam.teebay.entity;

import com.shazam.teebay.enums.RentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "RentalInfo", schema = "teebays")
@Data
public class RentBookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "rent_id", nullable = false)
    private Long rentId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "total_rent", nullable = false)
    private double totalRent;

    @Column(name = "rent_start_time", nullable = false)
    private LocalDateTime rentStartTime;

    @Column(name = "rent_end_time", nullable = false)
    private LocalDateTime rentEndTime;

    @Column(name = "rent_period", nullable = false)
    private int rentPeriod;

    @Column(name = "rent_time_unit", nullable = false)
    private String rentTimeUnit;  // "hours" or "days"

    @Enumerated(EnumType.STRING)
    @Column(name = "rent_status", nullable = false)
    private RentStatus rentStatus;

    @PrePersist
    protected void onCreate() {
        if (this.rentStatus == null) {
            this.rentStatus = RentStatus.BOOKED;
        }
    }
}
