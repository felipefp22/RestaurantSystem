package com.RestaurantSystem.Services.WebRequests.IFoodDTOs;

public record ReturnIFoodCodeToUserDTO(
        String userCode,
        String lastGeneratedFriendlyUrlUserCode
) {
}
