package com.shazam.teebay.entity;

import com.shazam.teebay.enums.TypeOfRent;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "Rent", schema = "teebays")
@Data
public class Rent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private double rentPrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOfRent typeOfRent;
    private String rentBufferPeriod;
}
