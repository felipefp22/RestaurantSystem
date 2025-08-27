package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record ConfirmOrCancelOrderDTO(
        UUID orderID,
        String adminPassword
) {
}
