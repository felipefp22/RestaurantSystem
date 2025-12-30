package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.CompanyEmployees;
import com.RestaurantSystem.Entities.Customer.Customer;
import com.RestaurantSystem.Entities.Printer.PrintRules;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Printer.Printer;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.Shift.DTOs.ShiftDTO;
import com.RestaurantSystem.Entities.Shift.Shift;

import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;
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
        Set<ProductCategory> productsCategories,
        Set<Customer> customers,
        ShiftDTO currentShift,
        List<CompanyEmployeesDTO> employees,
        int numberOfTables,
        Integer taxServicePercentage,
        Boolean deliveryHasServiceTax,
        Boolean pickupHasServiceTax,
        Integer maxRecommendedDistanceKM,
        Integer maxDeliveryDistanceKM,
        Integer baseDeliveryDistanceKM,
        Double baseDeliveryTax,
        Double taxPerExtraKM,
        List<String> noUserDeliveryMans,
        Set<Printer> printers,
        Set<PrintRules> printRules,
        Set<PrintSync> printSync

) {

    public CompanyOperationDTO(Company company, ShiftDTO currentShift) {
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
                company.getPickupHasServiceTax(),
                company.getMaxRecommendedDistanceKM(),
                company.getMaxDeliveryDistanceKM(),
                company.getBaseDeliveryDistanceKM(),
                company.getBaseDeliveryTax(),
                company.getTaxPerExtraKM(),
                company.getNoUserDeliveryMans(),
                company.getPrinters(),
                company.getPrintRules(),
                company.getPrintSync()
        );
    }

    public CompanyOperationDTO(CompanyOperationDeliveryManDTO company) {
        this(
                company.id(),
                company.ownerID(),
                company.companyName(),
                company.companyEmail(),
                company.companyPhone(),
                company.companyAddress(),
                company.companyLat(),
                company.companyLng(),
                company.urlCompanyLogo(),
                null,
                null,
                company.currentShift(),
                company.employees(),
                0,
                null,
                null,
                null,
                null,
                null,
                company.baseDeliveryDistanceKM(),
                company.baseDeliveryTax(),
                company.taxPerExtraKM(),
                null,
                null,
                null,
                null
        );
    }
}
