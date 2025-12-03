package com.RestaurantSystem.Services.WebRequests.IFoodDTOs;

public record IFoodRequestTokenDTO(
        String grantType,
        String clientId,
        String clientSecret,
        String authorizationCode,
        String authorizationCodeVerifier
) {
}
