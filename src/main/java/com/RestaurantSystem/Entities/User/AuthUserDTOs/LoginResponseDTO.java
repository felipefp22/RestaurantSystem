package com.RestaurantSystem.Entities.User.AuthUserDTOs;


import com.RestaurantSystem.Entities.CompaniesCompound.DTOs.CompoundResumeDTO;
import com.RestaurantSystem.Entities.Company.DTOs.CompanyResumeDTO;
import com.RestaurantSystem.Entities.ENUMs.Role;
import com.RestaurantSystem.Entities.User.AuthUserLogin;

import java.util.List;
import java.util.UUID;


public record LoginResponseDTO(
        String token_type,
        String access_token,
        UUID refresh_token,
        Boolean isEmailConfirmed,
        Boolean isPhoneConfirmed,
        Boolean isAdmAuthenticated,
        List<CompoundResumeDTO> compoundsYouAreOwner,
        List<CompanyResumeDTO> companiesYouWorks

) {
    public LoginResponseDTO (AuthUserLogin authUserLogin, String access_token, UUID refresh_token, Boolean isEmailConfirmed, Boolean isPhoneConfirmed,
                             List<CompoundResumeDTO> compoundsYouAreOwner, List<CompanyResumeDTO> companiesYouWorks) {
        this("Bearer",
                access_token,
                refresh_token,
                isEmailConfirmed,
                isPhoneConfirmed,
                (authUserLogin.getRole() == Role.ADMIN || authUserLogin.getRole() == Role.MASTERADMIN),
                compoundsYouAreOwner,
                companiesYouWorks
                );
    }

}
