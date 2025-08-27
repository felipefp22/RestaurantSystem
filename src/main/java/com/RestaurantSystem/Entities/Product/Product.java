package com.RestaurantSystem.Entities.Product;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Company company;

    private String name;
    private double price;
    private String description;
    private String imagePath;

    private String category;

    // <>------------ Constructors ------------<>
    public Product() {
    }
    public Product(CreateOrUpdateProductDTO productToCreate, Company company, String category) {
        this.company = company;
        this.name = productToCreate.name();
        this.price = productToCreate.price();
        this.description = productToCreate.description();
        this.category = category;
    }
}
