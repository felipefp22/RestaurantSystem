package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record ProductsToAddOnOrderDTO(
        UUID companyID,
        UUID orderID,
        List<ProductsToAddDTO> orderItemsIDs,
        List<CustomOrderItemsDTO> customOrderItems
        ) {
    public record ProductsToAddDTO(
            UUID productID,
            int quantity
    ) {
    }

    public record CustomOrderItemsDTO(
            List<String> productID,
            int quantity
    ) {
    }
}
