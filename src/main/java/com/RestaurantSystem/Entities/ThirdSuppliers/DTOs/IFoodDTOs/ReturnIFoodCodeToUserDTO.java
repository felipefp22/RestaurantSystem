package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

public record ReturnIFoodCodeToUserDTO(
        String userCode,
        String lastGeneratedFriendlyUrlUserCode
) {
}
