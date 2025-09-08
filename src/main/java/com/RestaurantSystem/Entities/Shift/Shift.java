package com.RestaurantSystem.Entities.Shift;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Entity
public class Shift {

    @Id
    private String id;

    @JsonIgnore
    @ManyToOne
    private Company company;

    private String shiftNumber;

    private LocalDateTime startTimeUTC;
    private LocalDateTime endTimeUTC;

    @OneToMany
    private List<Order> orders;

    //<>------------ Constructors ------------<>
    public Shift() {
    }
    public Shift(Company company, String shiftNumber) {
        this.id = company.getId().toString() + "_" + shiftNumber;
        this.company = company;
        this.shiftNumber = shiftNumber;
        this.startTimeUTC = LocalDateTime.now(ZoneOffset.UTC);
        this.endTimeUTC = null;
        this.orders = List.of();
    }


    //<>------------ Getters and setters ------------<>

    public String getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getShiftNumber() {
        return shiftNumber;
    }

    public LocalDateTime getStartTimeUTC() {
        return startTimeUTC;
    }

    public LocalDateTime getEndTimeUTC() {
        return endTimeUTC;
    }

    public void setEndTimeUTC(LocalDateTime endTimeUTC) {
        this.endTimeUTC = endTimeUTC;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
