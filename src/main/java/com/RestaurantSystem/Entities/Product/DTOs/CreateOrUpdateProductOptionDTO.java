package com.RestaurantSystem.Entities.Product.DTOs;

import java.util.UUID;

public record CreateOrUpdateProductOptionDTO(
        UUID companyID,
        UUID productOptID,
        String name,
        double price,
        String description,
        String imagePath,
        String productCategoryID,
        String ifoodCode
) {
}
