package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record ChangeOrderTableDTO(
        UUID orderID,
        String tableNumberOrDeliveryOrPickup,
        UUID customerID,
        String pickupName,
        String notes
) {

}

