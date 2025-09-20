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
//    @GetMapping("/get-all-products")
//    public ResponseEntity<List<Product>> getAllProducts(@RequestHeader("Authorization") String authorizationHeader) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//
//        var response = productService.getAllProducts(requesterID);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/get-products-by-category/{category}")
//    public ResponseEntity<List<Product>> getProductsByCategory(@RequestHeader("Authorization") String authorizationHeader,
//                                                              @PathVariable String category) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        var response = productService.getProductsByCategory(requesterID, category);
//
//        return ResponseEntity.ok(response);
//    }

//    @GetMapping("/get-products-by-category")
//    public ResponseEntity<Product> getProductById(@RequestHeader("Authorization") String authorizationHeader,
//                                                  @RequestBody FindProductDTO dto) {
//        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
//        var response = productService.getProductById(requesterID, dto);
//
//        return ResponseEntity.ok(response);
//    }

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

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity deleteProduct(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody FindProductDTO dto) {
        String requesterID = retriveAuthInfosService.retrieveEmailOfUser(authorizationHeader);
        productService.deleteProduct(requesterID, dto);

        return ResponseEntity.noContent().build();
    }
}
