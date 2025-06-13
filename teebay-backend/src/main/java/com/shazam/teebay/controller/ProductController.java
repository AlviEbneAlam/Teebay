package com.shazam.teebay.controller;

import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.dto.RegisterResponse;
import com.shazam.teebay.dto.UserInfoRec;
import com.shazam.teebay.service.ProductService;
import com.shazam.teebay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;

public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController( ProductService productService) {
        this.productService = productService;
    }

    @MutationMapping
    public AddProductResponse addProduct(@Argument AddProductRequest addProductRequest) {
        return productService.addProduct(addProductRequest);
    }
}
