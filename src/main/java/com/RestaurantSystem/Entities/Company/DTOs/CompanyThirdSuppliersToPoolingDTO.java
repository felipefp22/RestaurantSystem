package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.CompanyIfood;

import java.util.UUID;

public record CompanyThirdSuppliersToPoolingDTO(
        UUID companyId,
        CompanyIfood companyIfoodData
) {
}
