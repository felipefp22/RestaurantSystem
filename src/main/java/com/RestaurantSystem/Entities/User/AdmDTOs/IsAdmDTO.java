package com.RestaurantSystem.Entities.User.AdmDTOs;

public record IsAdmDTO(
        Boolean isAdmAuthenticated,
        Boolean isAdmMasterAuthenticated
) {

}
