package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.ProductCategory;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {
    private final ProductCategoryRepo productCategoryRepo;

    public ProductCategoryService(ProductCategoryRepo productCategoryRepo) {
        this.productCategoryRepo = productCategoryRepo;
    }

    // <> ---------- Methods ---------- <>
    public Set<ProductCategory> getAllCategories() {
        Set<ProductCategory> response = new HashSet<>(productCategoryRepo.findAll());

        return response;
    }

    public ProductCategory createCategory(String categoryName) {
        ProductCategory productCategory = new ProductCategory(categoryName);

        productCategoryRepo.save(productCategory);
        return productCategory;
    }

    public ProductCategory updateCategory(String categoryName, String newCategoryName) {
        ProductCategory productCategory = productCategoryRepo.findById(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ProductCategory response = null;
        if (productCategory != null) {
            productCategoryRepo.delete(productCategory);

            productCategory = productCategoryRepo.save(new ProductCategory(newCategoryName));
        }

        return productCategory;
    }
}
