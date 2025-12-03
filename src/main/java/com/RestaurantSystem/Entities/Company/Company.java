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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_or_open_shift")
    private Shift lastOrOpenShift;

    private int numberOfTables;

    private Integer taxServicePercentage = 10;

    private Integer maxRecommendedDistanceKM = 8;
    private Integer maxDeliveryDistanceKM = 20;

    private Integer baseDeliveryDistanceKM = 2;
    private Double baseDeliveryTax = 4.00;
    private Double taxPerExtraKM = 2.00;

    private Boolean pickupHasServiceTax = false;
    private Boolean deliveryHasServiceTax = false;

    private List<String> noUserDeliveryMans = new ArrayList<>();

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "company_ifood_id")
    private CompanyIFood companyIFoodData;

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

        this.companyLat = createCompanyDTO.lat();
        this.companyLng = createCompanyDTO.lng();
        this.employees = new ArrayList<>();;
        this.productsCategories = new ArrayList<>();;
        this.customers = new ArrayList<>();;
        this.shifts = new ArrayList<>();;
        this.numberOfTables = createCompanyDTO.numberOfTables();
        this.noUserDeliveryMans.add("DeliveryMan1");
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

    public Integer getTaxServicePercentage() {
        return taxServicePercentage;
    }
    public void setTaxServicePercentage(int taxServicePercentage) {
        if(taxServicePercentage <= 0 || taxServicePercentage > 100){
            throw new IllegalArgumentException("Tax service percentage must be between 0 and 100");
        }

        this.taxServicePercentage = taxServicePercentage;
    }

    public Integer getMaxRecommendedDistanceKM() {
        return maxRecommendedDistanceKM;
    }
    public void setMaxRecommendedDistanceKM(Integer maxRecommendedDistanceKM) {
        this.maxRecommendedDistanceKM = maxRecommendedDistanceKM;
    }
    public Integer getMaxDeliveryDistanceKM() {
        return maxDeliveryDistanceKM;
    }
    public void setMaxDeliveryDistanceKM(Integer maxDeliveryDistanceKM) {
        this.maxDeliveryDistanceKM = maxDeliveryDistanceKM;
    }

    public Integer getBaseDeliveryDistanceKM() {
        return baseDeliveryDistanceKM;
    }
    public void setBaseDeliveryDistanceKM(Integer baseDeliveryDistanceKM) {
        this.baseDeliveryDistanceKM = baseDeliveryDistanceKM;
    }
    public Double getBaseDeliveryTax() {
        return baseDeliveryTax;
    }
    public void setBaseDeliveryTax(Double baseDeliveryTax) {
        this.baseDeliveryTax = baseDeliveryTax;
    }
    public Double getTaxPerExtraKM() {
        return taxPerExtraKM;
    }
    public void setTaxPerExtraKM(Double taxPerExtraKM) {
        this.taxPerExtraKM = taxPerExtraKM;
    }

    public Boolean getPickupHasServiceTax() {
        return pickupHasServiceTax;
    }

    public void setPickupHasServiceTax(Boolean pickupHasServiceTax) {
        this.pickupHasServiceTax = pickupHasServiceTax;
    }

    public Boolean getDeliveryHasServiceTax() {
        return deliveryHasServiceTax;
    }

    public void setDeliveryHasServiceTax(Boolean deliveryHasServiceTax) {
        this.deliveryHasServiceTax = deliveryHasServiceTax;
    }

    public List<String> getNoUserDeliveryMans() {
        return noUserDeliveryMans;
    }
    public void setNoUserDeliveryMans(List<String> noUserDeliveryMans) {
        this.noUserDeliveryMans = noUserDeliveryMans;
    }
    public void addNoUserDeliveryMan(String deliveryManName) {
        this.noUserDeliveryMans.add(deliveryManName);
    }
    public void removeNoUserDeliveryMan(String deliveryManName) {
        this.noUserDeliveryMans.remove(deliveryManName);
    }

    public CompanyIFood getCompanyIFoodData() {
        return companyIFoodData;
    }

    public void setCompanyIFoodData(CompanyIFood companyIFoodData) {
        this.companyIFoodData = companyIFoodData;
    }
}
