package com.RestaurantSystem.Services;

import com.RestaurantSystem.Repositories.ProductRepo;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    // <> ---------- Methods ---------- <>

}
