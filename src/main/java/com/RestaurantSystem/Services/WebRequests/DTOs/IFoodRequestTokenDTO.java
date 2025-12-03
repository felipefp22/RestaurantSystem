package com.RestaurantSystem.Services.WebRequests.DTOs;

public record IFoodRequestTokenDTO(
        String grantType,
        String clientId,
        String clientSecret,
        String authorizationCode,
        String authorizationCodeVerifier
) {
}
