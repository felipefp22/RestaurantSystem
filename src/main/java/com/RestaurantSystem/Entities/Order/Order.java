package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.DTOs.CreateOrderDTO;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @ManyToOne
    private Shift shift;

    private int orderNumberOnShift;
    private String tableNumberOrDeliveryOrPickup;


    @ManyToOne
    private Customer customer;

    private String pickupName;

    private LocalDateTime openOrderDateUtc;
    private LocalDateTime closedWaitingPaymentAtUtc;
    private LocalDateTime completedOrderDateUtc;

    @ManyToOne
    private AuthUserLogin openedByUser;

    @ManyToOne
    private AuthUserLogin completedByUser;

    @ManyToOne
    private AuthUserLogin ifCanceledAuthorizedByUser;

    private double price;
    private double serviceTax;
    private double discount;
    private Double deliveryTax;
    private double totalPrice;
    private String notes;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersItems> orderItems = new ArrayList<>();

    @OneToMany
    private List<OrdersItemsCancelled> orderItemsCancelled;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderPrintSync> printSyncs = new ArrayList<>();

    private String deliveryManID;
    private List<UUID> deliveryOrdersSequence;

    // <>------------ Constructors ------------<>
    public Order() {
    }

    public Order(AuthUserLogin requester, Shift shift, int orderNumberOnShift, CreateOrderDTO createOrderDTO, Customer customer) {
        this.shift = shift;
        this.orderNumberOnShift = orderNumberOnShift;
        this.tableNumberOrDeliveryOrPickup = createOrderDTO.tableNumberOrDeliveryOrPickup();
        this.customer = customer;
        this.pickupName = createOrderDTO.pickupName();
        this.openOrderDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.openedByUser = requester;
        this.notes = createOrderDTO.notes();
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

    public void setTableNumberOrDeliveryOrPickup(String tableNumberOrDeliveryOrPickup) {
        this.tableNumberOrDeliveryOrPickup = tableNumberOrDeliveryOrPickup;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getPickupName() {
        return pickupName;
    }

    public void setPickupName(String pickupName) {
        this.pickupName = pickupName;
    }

    public LocalDateTime getOpenOrderDateUtc() {
        return openOrderDateUtc;
    }

    public LocalDateTime getClosedWaitingPaymentAtUtc() {
        return closedWaitingPaymentAtUtc;
    }

    public void setClosedWaitingPaymentAtUtc(LocalDateTime closedWaitingPaymentAtUtc) {
        this.closedWaitingPaymentAtUtc = closedWaitingPaymentAtUtc;
    }

    public LocalDateTime getCompletedOrderDateUtc() {
        return completedOrderDateUtc;
    }

    public void setCompletedOrderDateUtc(LocalDateTime completedOrderDateUtc) {
        this.completedOrderDateUtc = completedOrderDateUtc;
    }

    public AuthUserLogin getOpenedByUser() {
        return openedByUser;
    }

    public AuthUserLogin getCompletedByUser() {
        return completedByUser;
    }

    public void setCompletedByUser(AuthUserLogin completedByUser) {
        this.completedByUser = completedByUser;
    }

    public AuthUserLogin getIfCanceledAuthorizedByUser() {
        return ifCanceledAuthorizedByUser;
    }

    public void setIfCanceledAuthorizedByUser(AuthUserLogin ifCanceledAuthorizedByUser) {
        this.ifCanceledAuthorizedByUser = ifCanceledAuthorizedByUser;
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

    public Double getDeliveryTax() {
        return deliveryTax;
    }

    public void setDeliveryTax(Double deliveryTax) {
        this.deliveryTax = deliveryTax;
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

    public List<OrdersItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrdersItems> orderItems) {
        this.orderItems = orderItems;
    }

    public List<OrdersItemsCancelled> getOrderItemsCancelled() {
        if(orderItemsCancelled == null){
            orderItemsCancelled = new ArrayList<>();
        }
        return orderItemsCancelled;
    }

    public List<OrderPrintSync> getPrintSyncs() {
        return printSyncs;
    }

    public void addPrintSync(OrderPrintSync printSync) {
        this.printSyncs.add(printSync);
    }

    public void removePrintSync(OrderPrintSync printSync) {
        this.printSyncs.remove(printSync);
    }

    public String getDeliveryManID() {
        return deliveryManID;
    }

    public void setDeliveryManID(String deliveryManID) {
        this.deliveryManID = deliveryManID;
    }

    public List<UUID> getDeliveryOrdersSequence() {
        return deliveryOrdersSequence;
    }

    public void setDeliveryOrdersSequence(List<UUID> deliveryOrdersSequence) {
        this.deliveryOrdersSequence = deliveryOrdersSequence;
    }
}

