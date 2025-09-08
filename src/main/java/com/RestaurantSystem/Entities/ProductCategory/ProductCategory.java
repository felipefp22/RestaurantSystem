package com.RestaurantSystem.Entities.ProductCategory;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.ProductCategory.DTOs.CreateProductCategoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Company company;

    @OneToMany(mappedBy = "productCategory")
    private List<Product> products;

    private String categoryName;
    private String description;

    //<>------------ Constructors ------------<>
    public ProductCategory() {
    }

    public ProductCategory(CreateProductCategoryDTO createDTO, Company company) {
        this.company = company;
        this.categoryName = createDTO.categoryName();
        this.description = createDTO.description();
    }


    //<>------------ Getters and setters ------------<>

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Product> getProducts() {
        return products;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
