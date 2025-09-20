package com.RestaurantSystem.Controllers;

import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.DTOs.FindProductDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Infra.auth.RetriveAuthInfosService;
import com.RestaurantSystem.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductsController {
    private final ProductService productService;
    private final RetriveAuthInfosService retriveAuthInfosService;

    public ProductsController(ProductService productService, RetriveAuthInfosService retriveAuthInfosService) {
        this.productService = productService;
        this.retriveAuthInfosService = retriveAuthInfosService;
    }

    // <>------------ Methods ------------<>

    @PostMapping("/create-product")
    public ResponseEntity<Product> createProduct(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CreateOrUpdateProductDTO productToCreate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = productService.createProduct(requesterID, productToCreate);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-product")
    public ResponseEntity<Product> updateProduct(@RequestHeader("Authorization") String authorizationHeader,
                                                 @RequestBody CreateOrUpdateProductDTO productToUpdate) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        var response = productService.updateProduct(requesterID, productToUpdate);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-product/{productID}")
    public ResponseEntity deleteProduct(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody FindProductDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        productService.deleteProduct(requesterID, dto);

        return ResponseEntity.noContent().build();
    }
}
