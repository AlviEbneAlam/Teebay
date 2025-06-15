package com.shazam.teebay.Config;

import com.shazam.teebay.entity.Category;
import com.shazam.teebay.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryDataInitializer {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void insertDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                    new Category("Electronics"),
                    new Category("Home Appliances"),
                    new Category("Furniture"),
                    new Category("Sporting Goods"),
                    new Category("Outdoor"),
                    new Category("Toys")
            );

            categoryRepository.saveAll(defaultCategories);
        }
    }
}
