package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductOptionDTO;
import com.RestaurantSystem.Entities.Product.DTOs.FindProductDTO;
import com.RestaurantSystem.Entities.Product.DTOs.FindProductOptionDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.AddOrRemoveProductOptToProductCategoryDTO;
import com.RestaurantSystem.Services.AuxsServices.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ProductOptionService;
import com.RestaurantSystem.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-option")
public class ProductsOptionController {
    private final ProductOptionService productOptionService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public ProductsOptionController(ProductOptionService productOptionService, RetriveAuthInfosService retriveAuthInfosService) {
        this.productOptionService = productOptionService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <>------------ Methods ------------<>

    @PostMapping("/create-product-option")
    public ResponseEntity<ProductOption> createProductOption(@RequestHeader("Authorization") String authorizationHeader,
                                                             @RequestBody CreateOrUpdateProductOptionDTO productToCreate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = productOptionService.createProductOption(requesterID, productToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-or-remove-to-category")
    public ResponseEntity<ProductOption> addOrRemoveProductOptToProductCategory(@RequestHeader("Authorization") String authorizationHeader,
                                                                                @RequestBody AddOrRemoveProductOptToProductCategoryDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = productOptionService.addOrRemoveProductOptToProductCategory(requesterID, dto);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-product-option")
    public ResponseEntity<ProductOption> updateProductOption(@RequestHeader("Authorization") String authorizationHeader,
                                                             @RequestBody CreateOrUpdateProductOptionDTO productToUpdate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = productOptionService.updateProductOption(requesterID, productToUpdate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/delete-product-option")
    public ResponseEntity deleteProductOption(@RequestHeader("Authorization") String authorizationHeader,
                                              @RequestBody FindProductOptionDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        productOptionService.deleteProductOption(requesterID, dto);

        return ResponseEntity.noContent().build();
    }
}
