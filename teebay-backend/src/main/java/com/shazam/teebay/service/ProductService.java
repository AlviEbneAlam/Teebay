package com.shazam.teebay.service;

import com.shazam.teebay.dto.AddProductRequest;
import com.shazam.teebay.dto.AddProductResponse;
import com.shazam.teebay.entity.Category;
import com.shazam.teebay.entity.Products;
import com.shazam.teebay.entity.Rent;
import com.shazam.teebay.enums.ListedFor;
import com.shazam.teebay.enums.TypeOfRent;
import com.shazam.teebay.exception.GraphQLDataProcessingException;
import com.shazam.teebay.exception.GraphQLValidationException;
import com.shazam.teebay.repository.CategoryRepository;
import com.shazam.teebay.repository.ProductRepository;
import com.shazam.teebay.repository.RentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RentRepository rentRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          RentRepository rentRepository){
        this.productRepository=productRepository;
        this.categoryRepository=categoryRepository;
        this.rentRepository=rentRepository;
    }

    public AddProductResponse addProduct(AddProductRequest request) {
        try {
            Products product = new Products();
            product.setTitle(request.title());
            product.setDescription(request.description());
            product.setListedFor(parseListedFor(request.listedFor()));
            product.setSellingPrice(request.sellingPrice());
            product.setAvailabilityStatus("AVAILABLE");

            // Set Categories
            Set<Category> categories = request.categoriesList().stream()
                    .map(name -> categoryRepository.findByName(name)
                            .orElseThrow(() -> new GraphQLValidationException("Category not found: " + name)))
                    .collect(Collectors.toSet());
            product.setCategories(categories);

            // Set Rent if applicable
            if (!request.typeOfRent().isBlank() && request.rent() > 0) {
                Rent rent = new Rent();
                rent.setRentPrice(request.rent());
                rent.setTypeOfRent(parseTypeOfRent(request.typeOfRent()));
                rent.setRentBufferPeriod(request.rentBufferPeriod());
                product.setRent(rent);
                rent.setProductId(product.getId()); // Will be set post-persist
            }

            Products saved = productRepository.save(product);

            return new AddProductResponse("200", "Product added successfully with ID: " + saved.getId());
        } catch (GraphQLValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLDataProcessingException("Failed to add product", e);
        }
    }

    private ListedFor parseListedFor(String value) {
        try {
            return ListedFor.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid listedFor value: " + value);
        }
    }

    private TypeOfRent parseTypeOfRent(String value) {
        try {
            return TypeOfRent.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GraphQLValidationException("Invalid typeOfRent value: " + value);
        }
    }
}
