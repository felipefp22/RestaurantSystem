package com.RestaurantSystem.Entities.ProductCategory;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.CustomOrderPriceRule;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
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

    @OneToMany(mappedBy = "productCategory")
    private List<ProductOption> productOptions;

    private String categoryName;
    private String description;

    private Integer customOrderAllowed;

    @Enumerated(EnumType.STRING)
    private CustomOrderPriceRule customOrderPriceRule;

    @Enumerated(EnumType.STRING)
    private PrintCategory printCategory;

    private Integer printPriority;

    //<>------------ Constructors ------------<>
    public ProductCategory() {
    }

    public ProductCategory(CreateProductCategoryDTO createDTO, Company company, Integer printPriority) {
        this.company = company;
        this.categoryName = createDTO.categoryName();
        this.description = createDTO.description();
        this.printPriority = printPriority;
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

    public void setProducts(List<Product> products) {
        this.products = products;
    }
    public List<ProductOption> getProductOptions() {
        return productOptions;
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

    public Integer getCustomOrderAllowed() {
        return customOrderAllowed;
    }

    public void setCustomOrderAllowed(Integer customOrderAllowed) {
        this.customOrderAllowed = customOrderAllowed;
    }

    public CustomOrderPriceRule getCustomOrderPriceRule() {
        return customOrderPriceRule;
    }

    public void setCustomOrderPriceRule(CustomOrderPriceRule customOrderPriceRule) {
        this.customOrderPriceRule = customOrderPriceRule;
    }

    public PrintCategory getPrintCategory() {
        return printCategory;
    }
    public void setPrintCategory(PrintCategory printCategory) {
        this.printCategory = printCategory;
    }

    public Integer getPrintPriority() {
        return printPriority;
    }

    public void setPrintPriority(Integer printPriority) {
        this.printPriority = printPriority;
    }
}
