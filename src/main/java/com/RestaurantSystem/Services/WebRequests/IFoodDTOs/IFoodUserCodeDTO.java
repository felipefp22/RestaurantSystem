package com.RestaurantSystem.Services.WebRequests.IFoodDTOs;

public record IFoodUserCodeDTO(
        String userCode,
        String authorizationCodeVerifier,
        String verificationUrl,
        String verificationUrlComplete,
        Integer expiresIn
) {
}
