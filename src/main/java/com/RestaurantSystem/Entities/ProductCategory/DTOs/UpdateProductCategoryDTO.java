package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record UpdateProductCategoryDTO(
        UUID id,
        String categoryName,
        String description

) {
}
