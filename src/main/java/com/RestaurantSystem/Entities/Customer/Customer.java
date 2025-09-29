package com.RestaurantSystem.Entities.Customer;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.DTOs.CreateOrUpdateCustomerDTO;
import com.RestaurantSystem.Entities.Order.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    private String customerName;
    private String phone;
    private String address;
    private String addressNumber;
    private String city;
    private String state;
    private String zipCode;
    private Double lat;
    private Double lng;
    private String complement;

    //<>------------ Constructors ------------<>
    public Customer() {
    }
    public Customer(Company company, CreateOrUpdateCustomerDTO createDTO) {
        this.company = company;
        this.orders = List.of();
        this.customerName = createDTO.customerName();
        this.phone = createDTO.phone();
        this.address = createDTO.address();
        this.addressNumber = createDTO.addressNumber();
        this.city = createDTO.city();
        this.state = createDTO.state();
        this.zipCode = createDTO.zipCode();
        this.lat = createDTO.lat();
        this.lng = createDTO.lng();
        this.complement = createDTO.complement();
    }

    //<>------------ Getters and setters ------------<>

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }
}
