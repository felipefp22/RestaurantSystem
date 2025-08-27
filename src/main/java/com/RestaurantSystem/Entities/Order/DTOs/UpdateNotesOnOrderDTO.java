package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record UpdateNotesOnOrderDTO(
        UUID orderId,
        String notes
) {
}
