package com.RestaurantSystem.Entities.Product.DTOs;

import java.util.UUID;

public record FindProductDTO(
        UUID companyID,
        UUID productID
) {
}
