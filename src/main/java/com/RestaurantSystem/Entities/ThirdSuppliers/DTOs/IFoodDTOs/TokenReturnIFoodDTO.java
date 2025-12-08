package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

public record TokenReturnIFoodDTO(
        String accessToken,
        String refreshToken,
        String type,
        Integer expiresIn
) {
}
