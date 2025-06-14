package com.shazam.teebay.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(Long id,
                         String title,
                         String description,
                         List<String> categories,
                         double sellingPrice,
                         Double rent,
                         String typeOfRent,
                         String availabilityStatus,
                         String createdAt) {
}
