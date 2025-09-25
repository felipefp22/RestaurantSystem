package com.RestaurantSystem.Entities.Company.DTOs;

import com.RestaurantSystem.Entities.Company.Company;

import java.util.UUID;

public record CompanyResumeDTO(
        UUID companyID,
        String companyName,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        Double companyLat,
        Double companyLng,
        String urlCompanyLogo
) {
    public CompanyResumeDTO(Company company) {
        this(
                company.getId(),
                company.getCompanyName(),
                company.getCompanyEmail(),
                company.getCompanyPhone(),
                company.getCompanyAddress(),
                company.getCompanyLat(),
                company.getCompanyLng(),
                company.getUrlCompanyLogo()
        );
    }
}
