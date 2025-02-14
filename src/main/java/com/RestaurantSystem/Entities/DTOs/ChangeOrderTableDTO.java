package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record ChangeOrderTableDTO(
        UUID orderId,
        int tableToGo
) {
}
