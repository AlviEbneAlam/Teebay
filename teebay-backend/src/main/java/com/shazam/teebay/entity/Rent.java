package com.shazam.teebay.entity;

import com.shazam.teebay.enums.TypeOfRent;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "rent")
@Data
public class Rent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double rentPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOfRent typeOfRent;
}
