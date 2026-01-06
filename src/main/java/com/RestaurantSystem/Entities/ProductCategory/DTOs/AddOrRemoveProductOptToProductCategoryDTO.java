package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record AddOrRemoveProductOptToProductCategoryDTO(
        UUID companyID,
        UUID productCategoryID,
        UUID productOptID,
        String action
) {
}
