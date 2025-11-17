package com.RestaurantSystem.Entities.Order.DTOs;

import com.RestaurantSystem.Entities.Order.OrdersItems;

import java.util.List;
import java.util.UUID;

public record UpdateNotesOnOrderDTO(
        UUID companyID,
        UUID orderID,
        String notes
) {

}
