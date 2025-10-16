package com.RestaurantSystem.Entities.Company;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "compound_id")
    private CompaniesCompound ownerCompound;

    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyAddress;
    private Double companyLat;
    private Double companyLng;

    private String urlCompanyLogo;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompanyEmployees> employees;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductCategory> productsCategories;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Customer> customers;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Shift> shifts;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_or_open_shift")
    private Shift lastOrOpenShift;

    private int numberOfTables;

    //<>------------ Constructors ------------<>
    public Company() {
    }

    public Company(CompaniesCompound ownerCompound, CreateCompanyDTO createCompanyDTO){
        this.ownerCompound = ownerCompound;
        this.companyName = createCompanyDTO.companyName();
        this.companyEmail = createCompanyDTO.companyEmail();
        this.companyPhone = createCompanyDTO.companyPhone();
        this.companyAddress = createCompanyDTO.companyAddress();
        this.urlCompanyLogo = createCompanyDTO.urlCompanyLogo();
        this.employees = new ArrayList<>();;
        this.productsCategories = new ArrayList<>();;
        this.customers = new ArrayList<>();;
        this.shifts = new ArrayList<>();;
        this.numberOfTables = createCompanyDTO.numberOfTables();
    }

    //<>------------ Getters and setters ------------<>

    public UUID getId() {
        return id;
    }

    public CompaniesCompound getOwnerCompound() {
        return ownerCompound;
    }

    public void setOwner(CompaniesCompound ownerCompound) {
        this.ownerCompound = ownerCompound;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public Double getCompanyLat() {
        return companyLat;
    }
    public void setCompanyLat(Double companyLat) {
        this.companyLat = companyLat;
    }
    public Double getCompanyLng() {
        return companyLng;
    }
    public void setCompanyLng(Double companyLng) {
        this.companyLng = companyLng;
    }

    public String getUrlCompanyLogo() {
        return urlCompanyLogo;
    }

    public void setUrlCompanyLogo(String urlCompanyLogo) {
        this.urlCompanyLogo = urlCompanyLogo;
    }

    public List<CompanyEmployees> getEmployees() {
        return employees;
    }

    public List<ProductCategory> getProductsCategories() {
        return productsCategories;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public Shift getLastOrOpenShift() {
        return lastOrOpenShift;
    }
    public void setLastOrOpenShift(Shift lastOrOpenShift) {
        this.lastOrOpenShift = lastOrOpenShift;
    }

    public int getNumberOfTables() {
        return numberOfTables;
    }
    public void setNumberOfTables(int numberOfTables) {
        this.numberOfTables = numberOfTables;
    }
}
