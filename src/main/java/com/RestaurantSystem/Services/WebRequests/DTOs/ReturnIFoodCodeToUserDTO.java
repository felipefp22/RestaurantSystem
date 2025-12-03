package com.RestaurantSystem.Services.WebRequests.DTOs;

public record ReturnIFoodCodeToUserDTO(
        String userCode,
        String lastGeneratedFriendlyUrlUserCode
) {
}
