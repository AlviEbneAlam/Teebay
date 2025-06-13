package com.shazam.teebay.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "RentalInfo", schema = "teebays")
@Data
public class RentalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private String renterId;
    private String typeOfBooking;
    private double totalRent;
    private Date rentStartTime;
    private Date rentEndTime;
    private String rentStatus;
}
