package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.util.List;
import java.util.UUID;

public record CompanyDTO(
        UUID id,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        String urlCompanyLogo,
        List<CompanyEmployeesDTO> employees,
        List<ProductCategory> productsCategories,
        List<Customer> customers,
        Shift lastOrOpenShift,
        int numberOfTables,
        String ownerID,
        Integer taxServicePercentage,
        Boolean deliveryHasServiceTax,
        Boolean pickupHasServiceTax
) {
    public CompanyDTO(Company company) {
        this(
                company.getId(),
                company.getCompanyName(),
                company.getCompanyEmail(),
                company.getCompanyPhone(),
                company.getCompanyAddress(),
                company.getUrlCompanyLogo(),
                company.getEmployees().stream().map(CompanyEmployeesDTO::new).toList(),
                company.getProductsCategories(),
                company.getCustomers(),
                company.getLastOrOpenShift(),
                company.getNumberOfTables(),
                company.getOwnerCompound().getOwner().getEmail(),
                company.getTaxServicePercentage(),
                company.getDeliveryHasServiceTax(),
                company.getPickupHasServiceTax()
        );
    }

}
