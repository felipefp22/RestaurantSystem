package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record UpdateNotesOnOrderDTO(
        UUID companyID,
        UUID orderID,
        String notes
) {
}
