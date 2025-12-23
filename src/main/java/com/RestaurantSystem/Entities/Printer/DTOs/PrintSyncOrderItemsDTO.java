package com.RestaurantSystem.Entities.Printer.DTOs;

import com.RestaurantSystem.Entities.Order.OrdersItems;

import java.util.List;

public class PrintSyncOrderItemsDTO {
    private List<String> productId;
    private Double productPrice;
    private List<String> productOptions;
    private String name;
    private int quantity;
    private double price;
    private Boolean isThirdSupplierPrice;
    private String description;
    private String imagePath;
    private String status;
    private String notes;

    // <>------------ Constructor ------------<>
    public PrintSyncOrderItemsDTO(){

    }
    public PrintSyncOrderItemsDTO(OrdersItems orderItem){
        this.productId = orderItem.getProductId();
        this.productPrice = orderItem.getProductPrice();
        this.productOptions = orderItem.getProductOptions();
        this.name = orderItem.getName();
        this.quantity = 1;
        this.price = orderItem.getPrice();
        this.isThirdSupplierPrice = orderItem.getIsThirdSupplierPrice();
        this.description = orderItem.getDescription();
        this.imagePath = orderItem.getImagePath();
        this.status = orderItem.getStatus();
        this.notes = orderItem.getNotes();
    }

    // <>------------ Getters and Setters ------------<>


    public List<String> getProductId() {
        return productId;
    }

    public void setProductId(List<String> productId) {
        this.productId = productId;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public List<String> getProductOptions() {
        return productOptions;
    }

    public void setProductOptions(List<String> productOptions) {
        this.productOptions = productOptions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Boolean getIsThirdSupplierPrice() {
        return isThirdSupplierPrice;
    }

    public void setThirdSupplierPrice(Boolean thirdSupplierPrice) {
        isThirdSupplierPrice = thirdSupplierPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
