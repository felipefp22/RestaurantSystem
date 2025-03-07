package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record CreateOrUpdateProductDTO(
        UUID id,
        String name,
        double price,
        String description,
        String imagePath,
        String category
) {
}
