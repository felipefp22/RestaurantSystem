package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.UpdateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Infra.auth.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/product-category")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public ProductCategoryController(ProductCategoryService productCategoryService, RetriveAuthInfosService retriveAuthInfosService) {
        this.productCategoryService = productCategoryService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <>------------ Methods ------------<>

    @GetMapping("/get-all-categories-of-company")
    public ResponseEntity<List<ProductCategory>> getAllProductAndProductCategories(@RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.getAllProductAndProductCategories(requesterID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-product-category/{categoryName}")
    public ResponseEntity<List<ProductCategory>> createProductCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                                       @RequestBody CreateProductCategoryDTO createDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.createProductCategory(requesterID, createDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-category/{categoryName}/new-name-toward/{newCategoryName}")
    public ResponseEntity<List<ProductCategory>> updateCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                                @RequestBody UpdateProductCategoryDTO updateDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.updateCategory(requesterID, updateDTO);

        return ResponseEntity.ok(response);
    }
}
