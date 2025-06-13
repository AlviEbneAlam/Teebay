package com.shazam.teebay.controller;

import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.dto.RegisterResponse;
import com.shazam.teebay.dto.UserInfoRec;
import com.shazam.teebay.service.ProductService;
import com.shazam.teebay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @MutationMapping
    public AddProductResponse addProduct(@Argument @Valid AddProductRequest addProductRequest) {
        return productService.addProduct(addProductRequest);
    }

    @MutationMapping
    public AddProductResponse editProduct(@Argument Long productId, @Argument @Valid AddProductRequest editRequest) {
        return productService.editProduct(productId, editRequest);
    }
}
