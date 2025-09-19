package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record ProductsToAddOnOrderDTO(
        UUID companyID,
        UUID orderId,
        List<ProductsToAddDTO> products
) {
    public record ProductsToAddDTO(
            UUID productId,
            int quantity
    ) {

    }
}
