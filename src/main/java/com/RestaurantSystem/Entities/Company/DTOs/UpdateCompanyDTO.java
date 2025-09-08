package com.RestaurantSystem.Entities.Company.DTOs;

import java.util.UUID;

public record UpdateCompanyDTO(
        UUID id,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        String urlCompanyLogo,
        int numberOfTables
) {
}
