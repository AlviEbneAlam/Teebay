package com.shazam.teebay.dto;

import java.util.List;

public record ProductPageDto(List<ProductDto> products,
                             int totalPages,
                             long totalElements,
                             int currentPage) {

}
