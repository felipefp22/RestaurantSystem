package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

public record RequestTokenIFoodDTO(
        String grantType,
        String clientId,
        String clientSecret,
        String authorizationCode,
        String authorizationCodeVerifier
) {
}
