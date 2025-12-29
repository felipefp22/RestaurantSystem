package com.RestaurantSystem.Entities.Order;

import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ENUMs.OrderStatus;
import com.RestaurantSystem.Entities.Order.DTOs.CreateOrderDTO;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.AddressThirdSpOrderDTO;
import com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.CreateThirdSpOrderDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    @OneToMany(mappedBy = "order")
    private List<OrdersItems> orderItems = new ArrayList<>();

    private String deliveryManID;
    private List<UUID> deliveryOrdersSequence;


    private ThirdSuppliersEnum isThirdSpOrder;
    private Boolean isUserEditBlocked;
    private Boolean isThirdSpAddr;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdSpOrderID;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double thirdSpLat;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double thirdSpLng;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdSpZipCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdSpAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdSpComplementAddress;

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

    public Order(Shift shift, int orderNumberOnShift, CreateThirdSpOrderDTO thirdSpDTO, AddressThirdSpOrderDTO addressDTO) {
        this.shift = shift;
        this.orderNumberOnShift = orderNumberOnShift;
        this.tableNumberOrDeliveryOrPickup = thirdSpDTO.tableNumberOrDeliveryOrPickup();
        this.customer = null;
        this.pickupName = thirdSpDTO.customerName();
        this.openOrderDateUtc = LocalDateTime.now(ZoneOffset.UTC);
        this.notes = thirdSpDTO.notes();
        this.status = OrderStatus.OPEN;
        this.isThirdSpOrder = thirdSpDTO.isThirdSpOrder();
        this.isUserEditBlocked = true;
        this.isThirdSpAddr = addressDTO.isThirdSpAddr();
        this.thirdSpOrderID = thirdSpDTO.thirdSpOrderID();
        this.thirdSpLat = addressDTO.lat();
        this.thirdSpLng = addressDTO.lng();
        this.thirdSpZipCode = addressDTO.zipCode();
        this.thirdSpAddress = addressDTO.address();
        this.thirdSpComplementAddress = addressDTO.complementAddress();
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

    public ThirdSuppliersEnum getIsThirdSpOrder() {
        return isThirdSpOrder;
    }

    public Boolean isUserEditBlocked() {
        return isUserEditBlocked;
    }

    public Boolean isThirdSpAddr() {
        return isThirdSpAddr;
    }

    public String getThirdSpOrderID() {
        return thirdSpOrderID;
    }

    public void setThirdSpOrderID(String thirdSpOrderID) {
        this.thirdSpOrderID = thirdSpOrderID;
    }

    public Double getThirdSpLat() {
        return thirdSpLat;
    }

    public void setThirdSpLat(Double thirdSpLat) {
        this.thirdSpLat = thirdSpLat;
    }

    public Double getThirdSpLng() {
        return thirdSpLng;
    }

    public void setThirdSpLng(Double thirdSpLng) {
        this.thirdSpLng = thirdSpLng;
    }

    public String getThirdSpZipCode() {
        return thirdSpZipCode;
    }

    public void setThirdSpZipCode(String thirdSpZipCode) {
        this.thirdSpZipCode = thirdSpZipCode;
    }

    public String getThirdSpAddress() {
        return thirdSpAddress;
    }

    public void setThirdSpAddress(String thirdSpAddress) {
        this.thirdSpAddress = thirdSpAddress;
    }

    public String getThirdSpComplementAddress() {
        return thirdSpComplementAddress;
    }

    public void setThirdSpComplementAddress(String thirdSpComplementAddress) {
        this.thirdSpComplementAddress = thirdSpComplementAddress;
    }
}

