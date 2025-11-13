package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record ChangeOrderTableDTO(
        UUID companyID,
        UUID orderID,
        String tableNumberOrDeliveryOrPickup,
        UUID customerID,
        String pickupName,
        String notes,
        Integer deliveryDistanceKM
) {

}

