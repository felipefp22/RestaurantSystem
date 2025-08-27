//package com.RestaurantSystem.Services;
//
//import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
//import com.RestaurantSystem.Entities.Product.Product;
//import com.RestaurantSystem.Repositories.ProductRepo;
//import org.springframework.stereotype.Service;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//@Service
//public class ProductService {
//    private final ProductRepo productRepo;
//
//    public ProductService(ProductRepo productRepo) {
//        this.productRepo = productRepo;
//    }
//
//    // <> ---------- Methods ---------- <>
//    public Set<Product> getAllProducts() {
//        Set<Product> response = new HashSet<>(productRepo.findAll());
//
//        return response;
//    }
//
//    public Set<Product> getProductsByCategory(String category) {
//        Set<Product> response = new HashSet<>(productRepo.findAllByCategory(category));
//
//        return response;
//    }
//
//    public Product getProductById(UUID productId) {
//        var response = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found."));
//
//        return response;
//    }
//
//    public Product createProduct(CreateOrUpdateProductDTO productToCreate) {
//
//        return productRepo.save(product);
//    }
//
//    public Product updateProduct(CreateOrUpdateProductDTO productToUpdateDTO) {
//
//        return productRepo.save(productToUpdate);
//
//    }
//
//    public void deleteProduct(UUID productId) {
//        Product productToDelete = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found."));
//        productRepo.delete(productToDelete);
//    }
//}
