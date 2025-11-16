package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record UpdateProductCategoryDTO(
        UUID companyID,
        UUID categoryID,
        String categoryName,
        String description,
        Integer customOrderAllowed,
        String customOrderPriceRule

) {
}
