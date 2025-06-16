package com.shazam.teebay.controller;

import com.shazam.teebay.dto.*;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.service.BuyService;
import com.shazam.teebay.service.ProductService;
import com.shazam.teebay.service.RentService;
import com.shazam.teebay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private  final RentService rentService;
    private  final BuyService buyService;

    @MutationMapping
    public AddProductResponse addProduct(@Argument @Valid AddProductRequest addProductRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Add product request received. User: {}",email );
        log.info("Received AddProductRequest: {}", addProductRequest);

        try{
            return productService.addProduct(addProductRequest);
        }
        catch(Exception ex){
            log.info("Exception in add product: {}", ex.getLocalizedMessage());
            throw new GraphQLDataProcessingException("Failed to add product", ex);
        }
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

    @QueryMapping
    public ProductPageDto allProductsPaginated(@Argument int page, @Argument int size) {
        try {
            return productService.getAllProductsByStatus(page, size);
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


    @MutationMapping
    public AddProductResponse buyProduct(@Argument Long productId, @Argument String status) {
        return buyService.buyProduct(productId, status);
    }

    @MutationMapping
    public AddProductResponse bookForRent(@Argument Long productId, @Argument String rentStart,
                                          @Argument String rentEnd, @Argument int noOfHours) {
        return rentService.bookForRent(productId, rentStart, rentEnd, noOfHours);
    }

    @QueryMapping
    public ProductDto productById(@Argument Long productId) {
        return productService.getProductById(productId);
    }

}
