package com.shazam.teebay.controller;

import com.shazam.teebay.dto.*;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.service.ProductService;
import com.shazam.teebay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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

    @QueryMapping
    public ProductPageDto productsByUserPaginated(@Argument int page, @Argument int size) {
        try {
            return productService.getProductsByUserPaginated(page, size);
        } catch (GraphQLValidationException | GraphQLDataProcessingException ex) {
            throw ex;
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to fetch paginated products", e);
        }
    }

    @MutationMapping
    public AddProductResponse deleteProduct(@Argument Long productId) {
        return productService.deleteProduct(productId);
    }

}
