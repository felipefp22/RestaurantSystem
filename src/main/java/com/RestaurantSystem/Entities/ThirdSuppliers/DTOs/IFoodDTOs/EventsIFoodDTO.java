package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

public record EventsIFoodDTO(
        String id,
        String code,
        String fullCode,
        String orderId,
        String merchantId,
        String createdAt,
        String salesChannel

        ) {
}
