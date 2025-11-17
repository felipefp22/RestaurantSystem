package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.Product.Product;
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

    private String name;
    private double price;
    private String description;
    private String imagePath;
    private String status;

    // <>------------ Constructors ------------<>

    public OrdersItems() {
    }
    public OrdersItems(Order order, Product product) {
        this.order = order;
        this.productId = List.of(product.getId().toString());
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.imagePath = product.getImagePath();
        this.status = "ACTIVE";
    }
    public OrdersItems(Order order, List<Product> products, Double price) {
        this.order = order;
        this.productId = products.stream().map(p -> p.getId().toString()).sorted().toList();
        this.name = products.stream().map(Product::getName).sorted().reduce((a, b) -> a + "/" + b).orElse("");
        this.price = price;
        this.description = products.stream().map(Product::getDescription).sorted().reduce((a, b) -> a + " / " + b).orElse("");
        this.imagePath = null;
        this.status = "ACTIVE";
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
}
