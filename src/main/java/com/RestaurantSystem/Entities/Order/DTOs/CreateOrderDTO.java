package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record CreateOrderDTO(
        UUID companyID,
        String tableNumberOrDeliveryOrPickup,
        UUID customerID,
        String pickupName,
        List<OrderItemDTO> orderItemsIDs,
        String notes
) {
    public record OrderItemDTO(
            UUID productID,
            int quantity
    ) {

    }
}
