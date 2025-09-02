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

    // <>------------ Getters and Setters ------------<>
    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
