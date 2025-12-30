package com.RestaurantSystem.Entities.Shift;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    private String employeeOpenedShift;

    private String employeeClosedShift;

    //<>------------ Constructors ------------<>
    public Shift() {
    }
    public Shift(Company company, String shiftNumber, AuthUserLogin manager) {
        this.id = company.getId().toString() + "_" + shiftNumber;
        this.company = company;
        this.shiftNumber = shiftNumber;
        this.startTimeUTC = LocalDateTime.now(ZoneOffset.UTC);
        this.endTimeUTC = null;
        this.employeeOpenedShift = manager.getEmail();
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

    public String getManagerWhoseOpenedShift() {
        return employeeOpenedShift;
    }

    public String getEmployeeClosedShift() {
        return employeeClosedShift;
    }

    public void setEmployeeClosedShift(String employeeClosedShift) {
        this.employeeClosedShift = employeeClosedShift;
    }
}
