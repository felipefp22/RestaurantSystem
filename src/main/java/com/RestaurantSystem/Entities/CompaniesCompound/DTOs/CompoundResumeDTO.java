package com.RestaurantSystem.Entities.CompaniesCompound.DTOs;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyResumeDTO;

import java.util.List;
import java.util.UUID;

public record CompoundResumeDTO(
        UUID compoundID,
        String compoundName,
        String compoundDescription,
        List<CompanyResumeDTO> companies

) {
    public CompoundResumeDTO(CompaniesCompound compound) {
        this(
                compound.getId(),
                compound.getCompoundName(),
                compound.getCompoundDescription(),
                compound.getCompanies().stream().map(CompanyResumeDTO::new).toList()
        );
    }
}
