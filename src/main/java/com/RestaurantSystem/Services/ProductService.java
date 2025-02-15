package com.RestaurantSystem.Services;

import com.RestaurantSystem.Entities.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product;
import com.RestaurantSystem.Entities.ProductCategory;
import com.RestaurantSystem.Repositories.ProductCategoryRepo;
import com.RestaurantSystem.Repositories.ProductRepo;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final ProductCategoryRepo productCategoryRepo;

    public ProductService(ProductRepo productRepo, ProductCategoryRepo productCategoryRepo) {
        this.productRepo = productRepo;
        this.productCategoryRepo = productCategoryRepo;
    }

    // <> ---------- Methods ---------- <>
    public Set<Product> getAllProducts() {
        Set<Product> response = new HashSet<>(productRepo.findAll());

        return response;
    }

    public Set<Product> getProductsByCategory(String category) {
        Set<Product> response = new HashSet<>(productRepo.findAllByCategory(category));

        return response;
    }

    public Product getProductById(UUID productId) {
        var response = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found."));

        return response;
    }

    public Product createProduct(CreateOrUpdateProductDTO productToCreate) {
        ProductCategory category = productCategoryRepo.findById(productToCreate.category()).orElseThrow(() -> new RuntimeException("Category not found."));
        Product product = new Product(productToCreate, category);

        return productRepo.save(product);
    }

    public Product updateProduct(CreateOrUpdateProductDTO productToUpdateDTO) {
        ProductCategory category = productCategoryRepo.findById(productToUpdateDTO.category()).orElseThrow(() -> new RuntimeException("Category not found."));
        Product productToUpdate = productRepo.findById(productToUpdateDTO.id()).orElseThrow(() -> new RuntimeException("Product not found."));
        productToUpdate.setName(productToUpdateDTO.name());
        productToUpdate.setPrice(productToUpdateDTO.price());
        productToUpdate.setDescription(productToUpdateDTO.description());
        productToUpdate.setImagePath(productToUpdateDTO.imagePath());
        productToUpdate.setCategory(category);

        return productRepo.save(productToUpdate);

    }

    public void deleteProduct(UUID productId) {
        Product productToDelete = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found."));
        productRepo.delete(productToDelete);
    }
}
