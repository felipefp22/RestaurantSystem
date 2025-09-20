package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record ProductsToAddOnOrderDTO(
        UUID companyID,
        UUID orderID,
        List<ProductsToAddDTO> orderItemsIDs
) {
    public record ProductsToAddDTO(
            UUID productID,
            int quantity
    ) {

    }
}
