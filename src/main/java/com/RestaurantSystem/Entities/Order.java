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
    @Setter private String notes;
    @Setter private double price;
    @Setter private double priceWithServiceTax;
    @Setter private double discount;
    @Setter private OrderStatus status;

    @OneToMany
    private List<Product> orderItems;

    // <>------------ Constructors ------------<>
    public Order(int orderNumberOnDay, int tableNumber,String notes, List<Product> orderItems) {
        this.orderNumberOnDay = orderNumberOnDay;
        this.tableNumber = tableNumber;
        this.openOrderDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.notes = notes;
        this.orderItems = orderItems;
        this.status = OrderStatus.OPEN;
    }


    // <>------------ Methods ------------<>
    public void addProducts(List<Product> products) {
        if (this.status == OrderStatus.OPEN) {
            this.orderItems.addAll(products);
        }
    }

    public void removeProducts(List<Product> products) {
        if (this.status == OrderStatus.OPEN) {
            this.orderItems.removeAll(products);
        }
    }
}

