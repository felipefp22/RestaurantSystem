package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record CreateOrUpdateProductDTO(
        UUID name,
        double price,
        String description,
        String imagePath,
        String category
) {
}
