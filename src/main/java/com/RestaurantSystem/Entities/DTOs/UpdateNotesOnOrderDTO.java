package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record UpdateNotesOnOrderDTO(
        UUID orderId,
        String notes
) {
}
