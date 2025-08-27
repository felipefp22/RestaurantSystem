package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record ChangeOrderTableDTO(
        UUID orderId,
        int tableToGo
) {
}
