package com.RestaurantSystem.Services.WebRequests.DTOs;

public record IFoodUserCodeDTO(
        String userCode,
        String authorizationCodeVerifier,
        String verificationUrl,
        String verificationUrlComplete,
        Integer expiresIn
) {
}
