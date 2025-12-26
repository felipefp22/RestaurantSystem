package com.RestaurantSystem.Entities.ProductCategory.DTOs;

import java.util.UUID;

public record SortPrintPriorityDTO(
        UUID companyID,
        UUID categoryID,
        String action
) {

}
