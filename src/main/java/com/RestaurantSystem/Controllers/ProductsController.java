package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product;
import com.RestaurantSystem.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductsController {
    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    // <>------------ Methods ------------<>
    @GetMapping("/get-all-products")
    public ResponseEntity<Set<Product>> getAllProducts() {
        var response = productService.getAllProducts();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-products-by-category/{category}")
    public ResponseEntity<Set<Product>> getProductsByCategory(@PathVariable String category) {
        var response = productService.getProductsByCategory(category);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-products-by-id/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID productId) {
        var response = productService.getProductById(productId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateOrUpdateProductDTO productToCreate) {
        var response = productService.createProduct(productToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-product")
    public ResponseEntity<Product> updateProduct(@RequestBody CreateOrUpdateProductDTO productToUpdate) {
        var response = productService.updateProduct(productToUpdate);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity deleteProduct(@PathVariable UUID productId) {

         productService.deleteProduct(productId);

         return ResponseEntity.noContent().build();
    }
}
