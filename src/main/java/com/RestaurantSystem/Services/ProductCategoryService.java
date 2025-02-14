package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.ProductCategory;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {
    private final ProductCategoryRepo productCategoryRepo;

    public ProductCategoryService(ProductCategoryRepo productCategoryRepo) {
        this.productCategoryRepo = productCategoryRepo;
    }

    // <> ---------- Methods ---------- <>
    public List<ProductCategory> getAllCategories() {
        List<ProductCategory> response = productCategoryRepo.findAll();

        return response;
    }

    public ProductCategory createCategory(String categoryName) {
        ProductCategory productCategory = new ProductCategory(categoryName);

        productCategoryRepo.save(productCategory);
        return productCategory;
    }

    public ProductCategory updateCategory(String categoryName, String newCategoryName) {
        ProductCategory productCategory = productCategoryRepo.findById(categoryName).orElseThrow();

        ProductCategory response = null;
        if (productCategory != null) {
            productCategoryRepo.delete(productCategory);

            ProductCategory newProductCategory = new ProductCategory(newCategoryName);
            response = productCategoryRepo.save(newProductCategory);
        }

        return response;
    }
}
