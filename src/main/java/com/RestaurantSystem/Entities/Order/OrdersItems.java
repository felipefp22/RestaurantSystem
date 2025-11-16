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
    private int quantity;

    // <>------------ Constructors ------------<>

    public OrdersItems() {
    }
    public OrdersItems(Order order, Product product, int quantity) {
        this.order = order;
        this.productId = List.of(product.getId().toString());
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.imagePath = product.getImagePath();
        this.quantity = quantity;
    }
    public OrdersItems(Order order, List<Product> products, Double price, int quantity) {
        this.order = order;
        this.productId = products.stream().map(p -> p.getId().toString()).toList();
        this.name = products.stream().map(Product::getName).reduce((a, b) -> a + "/" + b).orElse("");
        this.price = price;
        this.description = products.stream().map(Product::getDescription).reduce((a, b) -> a + " / " + b).orElse("");
        this.imagePath = null;
        this.quantity = quantity;
    }

    //Just Use to PrintSync bellow constructor
    public OrdersItems(OrdersItems orderItems, int quantity) {
        this.order = orderItems.getOrder();
        this.productId = orderItems.getProductId();
        this.name = orderItems.getName();
        this.price = orderItems.getPrice();
        this.description = orderItems.getDescription();
        this.imagePath = orderItems.getImagePath();
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
