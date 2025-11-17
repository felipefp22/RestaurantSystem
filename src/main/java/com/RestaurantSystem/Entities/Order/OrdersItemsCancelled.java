package com.RestaurantSystem.Entities.Order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class OrdersItemsCancelled {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private Double price;

    // <>------------ Constructors ------------<>
    public OrdersItemsCancelled() {

    }
    public OrdersItemsCancelled(OrdersItems orderItems) {
        this.name = orderItems.getName();
        this.price = orderItems.getPrice();
    }


    // <>------------ Getters and Setters ------------<>
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public Double getPrice() {
        return price;
    }
}