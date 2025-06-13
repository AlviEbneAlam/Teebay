package com.shazam.teebay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddProductRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotEmpty(message = "At least one category must be provided")
        List<@NotBlank(message = "Category name must not be blank") String> categoriesList,

        @Size(max = 1000, message = "Description can be at most 1000 characters")
        String description,

        @NotBlank(message = "listedFor is required (BUY, RENT, BOTH)")
        String listedFor,

        @PositiveOrZero(message = "Selling price must be zero or more")
        double sellingPrice,

        @PositiveOrZero(message = "Rent must be zero or more")
        double rent,

        String typeOfRent,

        String rentBufferPeriod
) {
}
