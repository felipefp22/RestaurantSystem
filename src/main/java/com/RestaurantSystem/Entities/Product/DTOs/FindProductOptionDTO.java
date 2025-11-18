package com.RestaurantSystem.Entities.Product.DTOs;

import java.util.UUID;

public record FindProductOptionDTO(
        UUID companyID,
        UUID productOptID
) {
}
