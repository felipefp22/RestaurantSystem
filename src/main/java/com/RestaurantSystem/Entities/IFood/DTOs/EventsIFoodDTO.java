package com.RestaurantSystem.Entities.IFood.DTOs;

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
