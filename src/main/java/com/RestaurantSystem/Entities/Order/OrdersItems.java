package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Product.ProductOption;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class OrdersItems {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Order order;

    private List<String> productId;
    private Double productPrice;
    private List<String> productOptions;

    private String name;
    private double price;
    private String description;
    private String imagePath;
    private String status;
    private String notes;

    // <>------------ Constructors ------------<>

    public OrdersItems() {
    }
    public OrdersItems(Order order, Product product, List<ProductOption> productOpts) {
        this.order = order;
        this.productId = List.of(product.getId().toString());
        this.productPrice = product.getPrice();
        this.productOptions = productOpts.stream().map(po -> po.getId().toString()+"|"+po.getName()+"|"+po.getPrice()).sorted().toList();
        this.name = product.getName();
        this.price = product.getPrice() + productOpts.stream().mapToDouble(ProductOption::getPrice).sum();
        this.description = product.getDescription();
        this.imagePath = product.getImagePath();
        this.status = "ACTIVE";
    }
    public OrdersItems(Order order, List<Product> products, Double productPrice, List<ProductOption> productOpts, String notes) {
        this.order = order;
        this.productId = products.stream().map(p -> p.getId().toString()).sorted().toList();
        this.productPrice = productPrice;
        this.productOptions = productOpts.stream().map(po -> po.getId().toString()+"|"+po.getName()+"|"+po.getPrice()).sorted().toList();
        this.name = products.stream().map(Product::getName).sorted().reduce((a, b) -> a + "/" + b).orElse("");
        this.price = productPrice + productOpts.stream().mapToDouble(ProductOption::getPrice).sum();
        this.description = products.stream().map(Product::getDescription).sorted().reduce((a, b) -> a + " / " + b).orElse("");
        this.imagePath = null;
        this.status = "ACTIVE";
        this.notes = notes;
    }

    /// <>------------ Getters and Setters ------------<>

    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public List<String> getProductId() {
        return productId;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public List<String> getProductOptions() {
        return productOptions;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
