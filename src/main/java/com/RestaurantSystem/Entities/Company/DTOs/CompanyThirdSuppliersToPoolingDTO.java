package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.CompanyIFood;

import java.util.UUID;

public record CompanyThirdSuppliersToPoolingDTO(
        UUID companyId,
        CompanyIFood companyIFoodData
) {
}
