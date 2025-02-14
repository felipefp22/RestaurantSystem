package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.ProductCategory;
import com.RestaurantSystem.Services.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-category")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    // <>------------ Methods ------------<>
    @GetMapping("/get-all-categories")
    public ResponseEntity<ProductCategory> getAllCategories() {
        var response = productCategoryService.getAllCategories();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-category/{categoryName}")
    public ResponseEntity<ProductCategory> createCategory(@PathVariable String categoryName) {
        var response = productCategoryService.createCategory(categoryName);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-category/{categoryName}/new-name-toward/{newCategoryName}")
    public ResponseEntity<ProductCategory> updateCategory(@PathVariable String categoryName, @PathVariable String newCategoryName) {
        var response = productCategoryService.updateCategory(categoryName, newCategoryName);

        return ResponseEntity.ok(response);
    }
}
