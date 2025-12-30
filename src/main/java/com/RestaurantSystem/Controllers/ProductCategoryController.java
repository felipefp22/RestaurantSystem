package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.ProductCategory.DTOs.ChangeNewProductsDefaultImgDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.SortPrintPriorityDTO;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.UpdateProductCategoryDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ProductCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @GetMapping("/get-all-categories-of-company/{companyID}")
    public ResponseEntity<Set<ProductCategory>> getAllProductAndProductCategories(@RequestHeader("Authorization") String authorizationHeader,
                                                                                   @PathVariable UUID companyID) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.getAllProductAndProductCategories(requesterID, companyID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-product-category")
    public ResponseEntity<Set<ProductCategory>> createProductCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                                       @RequestBody CreateProductCategoryDTO createDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.createProductCategory(requesterID, createDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-category")
    public ResponseEntity<Set<ProductCategory>> updateCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                                @RequestBody UpdateProductCategoryDTO updateDTO) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.updateCategory(requesterID, updateDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-default-new-products-img")
    public ResponseEntity<Set<ProductCategory>> changeDefaultNewProductsImage(@RequestHeader("Authorization") String authorizationHeader,
                                                                              @RequestBody ChangeNewProductsDefaultImgDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.changeDefaultNewProductsImage(requesterID, dto);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/sort-print-priority")
    public ResponseEntity<Set<ProductCategory>> sortPrintPriority(@RequestHeader("Authorization") String authorizationHeader,
                                                                @RequestBody SortPrintPriorityDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);

        var response = productCategoryService.sortPrintPriority(requesterID, dto);

        return ResponseEntity.ok(response);
    }
}
