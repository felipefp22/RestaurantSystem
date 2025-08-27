package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Shift.Shift;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Shift shift;

    private int orderNumberOnShift;
    private String tableNumberOrDeliveryOrPickup;

    private LocalDateTime openOrderDateUtc;
    private LocalDateTime completedOrderDateUtc;

    private double price;
    private double serviceTax;
    private double discount;
    private double totalPrice;
    private String notes;

    private OrderStatus status;

    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> orderItems = new ArrayList<>();

    // <>------------ Constructors ------------<>
    public Order() {
    }

    public Order(int orderNumberOnShift, String tableNumberOrDeliveryOrPickup, String notes, List<Product> orderItems) {
        this.orderNumberOnShift = orderNumberOnShift;
        this.tableNumberOrDeliveryOrPickup = tableNumberOrDeliveryOrPickup;
        this.openOrderDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.notes = notes;
        this.orderItems = orderItems;
        this.status = OrderStatus.OPEN;
    }


    // <>------------ Methods ------------<>

    public UUID getId() {
        return id;
    }
    public Shift getShift() {
        return shift;
    }

    public int getOrderNumberOnShift() {
        return orderNumberOnShift;
    }

    public String getTableNumberOrDeliveryOrPickup() {
        return tableNumberOrDeliveryOrPickup;
    }
    public LocalDateTime getOpenOrderDateUtc() {
        return openOrderDateUtc;
    }

    public LocalDateTime getCompletedOrderDateUtc() {
        return completedOrderDateUtc;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getServiceTax() {
        return serviceTax;
    }

    public void setServiceTax(double serviceTax) {
        this.serviceTax = serviceTax;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

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

