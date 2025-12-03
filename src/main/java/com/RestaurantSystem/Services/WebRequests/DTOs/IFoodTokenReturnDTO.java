package com.RestaurantSystem.Services.WebRequests.DTOs;

public record IFoodTokenReturnDTO(
        String accessToken,
        String refreshToken,
        String type,
        Integer expiresIn
) {
}
