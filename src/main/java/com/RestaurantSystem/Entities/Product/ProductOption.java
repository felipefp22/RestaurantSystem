package com.RestaurantSystem.Entities.Product;

import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductDTO;
import com.RestaurantSystem.Entities.Product.DTOs.CreateOrUpdateProductOptionDTO;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private ProductCategory productCategory;

    private String name;
    private double price;
    private String description;
    private String imagePath;

    private String ifoodCode;

    // <>------------ Constructors ------------<>
    public ProductOption() {
    }
    public ProductOption(CreateOrUpdateProductOptionDTO productOptToCreate, ProductCategory productCategory) {
        this.productCategory = productCategory;
        this.name = productOptToCreate.name();
        this.price = productOptToCreate.price();
        this.description = productOptToCreate.description();
        this.imagePath = productOptToCreate.imagePath();
        this.ifoodCode = productOptToCreate.ifoodCode();
    }

    // <>------------ Getters and Setters ------------<>
    public UUID getId() {
        return id;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }
    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
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

    public String getIfoodCode() {
        return ifoodCode;
    }

    public void setIfoodCode(String ifoodCode) {
        this.ifoodCode = ifoodCode;
    }
}
