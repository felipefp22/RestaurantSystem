package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Infra.auth.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Set<String>> getAllProductAndProductCategories(@RequestHeader("Authorization") String authorizationHeader) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.getAllProductAndProductCategories(requesterID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-product-category/{categoryName}")
    public ResponseEntity<Set<String>> createProductCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                             @PathVariable String categoryName) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.createProductCategory(requesterID, categoryName);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-category/{categoryName}/new-name-toward/{newCategoryName}")
    public ResponseEntity<Set<String>> updateCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                          @PathVariable String categoryName, @PathVariable String newCategoryName) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.updateCategory(requesterID, categoryName, newCategoryName);

        return ResponseEntity.ok(response);
    }
}
