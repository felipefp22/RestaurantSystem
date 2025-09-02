package com.RestaurantSystem.Entities.Company;

import com.RestaurantSystem.Entities.Company.DTOs.CreateCompanyDTO;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.Shift.Shift;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String owner;

    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyAddress;
    private String urlCompanyLogo;

    private List<String> managers;
    private List<String> employees;

    private List<String> productsCategories;

    @OneToMany(mappedBy = "company")
    private List<Product> products;

    @OneToMany(mappedBy = "company")
    private List<Customer> customers;

    @OneToMany(mappedBy = "company")
    private List<Shift> shifts;

    //<>------------ Constructors ------------<>
    public Company() {
    }

    public Company(CreateCompanyDTO createCompanyDTO, AuthUserLogin owner){
        this.owner = owner.getEmail();
        this.companyName = createCompanyDTO.companyName();
        this.companyEmail = createCompanyDTO.companyEmail();
        this.companyPhone = createCompanyDTO.companyPhone();
        this.companyAddress = createCompanyDTO.companyAddress();
        this.urlCompanyLogo = createCompanyDTO.urlCompanyLogo();
        this.managers = List.of();
        this.employees = List.of();
        this.productsCategories = List.of();
        this.products = List.of();
        this.customers = List.of();
        this.shifts = List.of();
    }

    //<>------------ Getters and setters ------------<>

    public UUID getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public String getUrlCompanyLogo() {
        return urlCompanyLogo;
    }

    public void setUrlCompanyLogo(String urlCompanyLogo) {
        this.urlCompanyLogo = urlCompanyLogo;
    }

    public List<String> getManagers() {
        return managers;
    }
    public List<String> addManager(String managerEmail) {
        this.managers.add(managerEmail);
        return this.managers;
    }
    public List<String> removeManager(String managerEmail) {
        this.managers.remove(managerEmail);
        return this.managers;
    }

    public List<String> getEmployees() {
        return employees;
    }
    public List<String> addEmployee(String employeeEmail) {
        this.employees.add(employeeEmail);
        return this.employees;
    }
    public List<String> removeEmployee(String employeeEmail) {
        this.employees.remove(employeeEmail);
        return this.employees;
    }

    public List<String> getProductsCategories() {
        return productsCategories;
    }

    public List<String> addProductsCategory(String category) {
        this.productsCategories.add(category);
        return this.productsCategories;
    }
    public List<String> removeProductsCategory(String category) {
        this.productsCategories.remove(category);
        return this.productsCategories;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Shift> getShifts() {
        return shifts;
    }
}
