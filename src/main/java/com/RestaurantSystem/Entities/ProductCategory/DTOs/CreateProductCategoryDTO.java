package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record CreateProductCategoryDTO(
        UUID companyID,
        String categoryName,
        String description

) {
}
