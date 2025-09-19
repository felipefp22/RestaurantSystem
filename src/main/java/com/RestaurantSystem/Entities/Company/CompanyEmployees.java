package com.RestaurantSystem.Entities.Company;

import com.RestaurantSystem.Entities.User.AuthUserLogin;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class CompanyEmployees {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    private Company company;

    @ManyToOne
    private AuthUserLogin employee;

    @Enumerated(EnumType.STRING)
    private EmployeePosition position;

    // <>------------ Constructors ------------<>
    public CompanyEmployees() {
    }
    public CompanyEmployees(Company company, AuthUserLogin employee, EmployeePosition position) {
        this.company = company;
        this.employee = employee;
        this.position = position;
    }

    // <>------------ Getters & Setters ------------<>

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public AuthUserLogin getEmployee() {
        return employee;
    }

    public EmployeePosition getPosition() {
        return position;
    }

    public void setPosition(EmployeePosition position) {
        this.position = position;
    }
}
