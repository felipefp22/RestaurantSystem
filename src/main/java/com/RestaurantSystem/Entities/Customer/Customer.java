package com.RestaurantSystem.Entities.Customer;

import com.RestaurantSystem.Entities.Company.Company;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.UUID;

@Entity
public class Customer {

    @Id
    private UUID id;

    @ManyToOne
    private Company company;

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


}
