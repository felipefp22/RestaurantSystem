package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record ConfirmOrCancelOrderDTO(
        UUID orderID,
        UUID managerID,
        String adminPassword,
        String cancellationReason
) {
}
