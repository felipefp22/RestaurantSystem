package com.RestaurantSystem.Entities.Company.DTOs;

public record CreateCompanyDTO(
    String companyName,
    String companyEmail,
    String companyPhone,
    String companyAddress,
    String urlCompanyLogo
) {
}
