package com.RestaurantSystem.Entities.CompaniesCompound.DTOs;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;

import java.util.List;
import java.util.UUID;

public record CompaniesCompoundDTO(
        UUID id,
        AuthUserLogin owner,
        String compoundName,
        String compoundDescription,
        List<CompanyDTO> companies
) {
    public CompaniesCompoundDTO(CompaniesCompound cc) {
        this(
                cc.getId(),
                cc.getOwner(),
                cc.getCompoundName(),
                cc.getCompoundDescription(),
                cc.getCompanies().stream().map(CompanyDTO::new).toList()
        );
    }
}
