package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.util.List;
import java.util.UUID;

public record CompanyOperationDTO(
        UUID id,
        String ownerID,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        Double companyLat,
        Double companyLng,
        String urlCompanyLogo,
        List<ProductCategory> productsCategories,
        List<Customer> customers,
        Shift currentShift,
        List<CompanyEmployeesDTO> employees,
        int numberOfTables,
        Integer taxServicePercentage,
        Boolean deliveryHasServiceTax,
        Boolean pickupHasServiceTax

) {

    public CompanyOperationDTO(Company company, Shift currentShift) {
        this(
                company.getId(),
                company.getOwnerCompound().getOwner().getEmail(),
                company.getCompanyName(),
                company.getCompanyEmail(),
                company.getCompanyPhone(),
                company.getCompanyAddress(),
                company.getCompanyLat(),
                company.getCompanyLng(),
                company.getUrlCompanyLogo(),
                company.getProductsCategories(),
                company.getCustomers(),
                currentShift,
                company.getEmployees().stream().map(CompanyEmployeesDTO::new).toList(),
                company.getNumberOfTables(),
                company.getTaxServicePercentage(),
                company.getDeliveryHasServiceTax(),
                company.getPickupHasServiceTax()
        );
    }
}
