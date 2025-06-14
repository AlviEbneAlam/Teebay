package com.shazam.teebay.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_category", schema = "teebays")
@Data
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;
}