package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record ChangeNewProductsDefaultImgDTO(
        UUID companyID,
        UUID categoryID,
        String defaultImageToNewProducts
) {
}
