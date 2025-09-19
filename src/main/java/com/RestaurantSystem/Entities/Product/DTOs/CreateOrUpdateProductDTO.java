package com.RestaurantSystem.Entities.Product.DTOs;

import java.util.UUID;

public record CreateOrUpdateProductDTO(
        UUID companyID,
        UUID productID,
        String name,
        double price,
        String description,
        String imagePath,
        String productCategoryID
) {
}
