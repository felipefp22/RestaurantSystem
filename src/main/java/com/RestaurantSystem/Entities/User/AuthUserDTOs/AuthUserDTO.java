package com.RestaurantSystem.Entities.User.AuthUserDTOs;

import com.RestaurantSystem.Entities.CompaniesCompound.CompaniesCompound;
import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CompaniesCompoundDTO;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyEmployeesDTO;
import com.RestaurantSystem.Entities.User.AuthUserLogin;

import java.util.List;

public record AuthUserDTO(
        String email,
        Boolean emailConfirmed,
        String name,
        String phone,
        String urlProfilePhoto,
        List<CompaniesCompoundDTO> companiesCompounds,
        List<CompanyEmployeesDTO> worksAtCompanies,
        Boolean hasOwnAdministrativePassword

) {
    public AuthUserDTO(AuthUserLogin user) {
        this(user.getEmail(),
                user.isEmailConfirmed(),
                user.getName(),
                user.getPhone(),
                user.getUrlProfilePhoto(),
                user.getCompaniesCompounds().stream().map(CompaniesCompoundDTO::new).toList(),
                user.getWorksAtCompanies().stream()
                        .map(CompanyEmployeesDTO::new)
                        .toList(),
                user.getOwnAdministrativePassword() != null
        );
    }

}
