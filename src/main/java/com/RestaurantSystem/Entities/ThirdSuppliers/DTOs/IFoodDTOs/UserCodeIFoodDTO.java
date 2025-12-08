package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

public record UserCodeIFoodDTO(
        String userCode,
        String authorizationCodeVerifier,
        String verificationUrl,
        String verificationUrlComplete,
        Integer expiresIn
) {
}
