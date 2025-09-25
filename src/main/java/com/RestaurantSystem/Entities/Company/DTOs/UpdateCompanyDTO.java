package com.RestaurantSystem.Entities.Company.DTOs;

import java.util.UUID;

public record UpdateCompanyDTO(
        UUID companyID,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        Double companyLat,
        Double companyLng,
        String urlCompanyLogo,
        int numberOfTables
) {
}
