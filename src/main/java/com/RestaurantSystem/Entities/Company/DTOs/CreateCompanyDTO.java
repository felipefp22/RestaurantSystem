package com.RestaurantSystem.Entities.Company.DTOs;

import java.util.UUID;

public record CreateCompanyDTO(
        UUID companiesCompoundID,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        String urlCompanyLogo,
        int numberOfTables
) {
}
