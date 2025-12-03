package com.RestaurantSystem.Services.WebRequests.IFoodDTOs;

public record IFoodTokenReturnDTO(
        String accessToken,
        String refreshToken,
        String type,
        Integer expiresIn
) {
}
