package com.RestaurantSystem.Entities;

import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private int orderNumberOnDay;
    @Setter private int tableNumber;
    private LocalDateTime openOrderDateUtc;
    @Setter private LocalDateTime completedOrderDateUtc;
    @Setter private String annotations;
    @Setter private double price;
    @Setter private double priceWithServiceTax;
    @Setter private double discount;
    @Setter private OrderStatus status;

    @OneToMany
    private List<Products> orderItems;

    // <>------------ Constructors ------------<>
    public Order(int orderNumberOnDay, int tableNumber, List<Products> orderItems) {
        this.orderNumberOnDay = orderNumberOnDay;
        this.tableNumber = tableNumber;
        this.openOrderDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.orderItems = orderItems;
        this.status = OrderStatus.OPEN;
    }


    // <>------------ Methods ------------<>
    public void addProducts(List<Products> products) {
        this.orderItems.addAll(products);
    }
    public void removeProducts(List<Products> products) {
        this.orderItems.removeAll(products);
    }
}

