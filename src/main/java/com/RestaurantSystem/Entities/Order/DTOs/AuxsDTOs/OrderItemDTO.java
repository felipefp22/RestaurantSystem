package com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs;

import java.util.UUID;

public record OrderItemDTO(
        UUID productID,
        int quantity
) {
}
