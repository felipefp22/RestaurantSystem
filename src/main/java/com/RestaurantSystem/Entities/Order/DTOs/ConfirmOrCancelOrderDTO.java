package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record ConfirmOrCancelOrderDTO(
        UUID companyID,
        UUID orderID,
        String managerID,
        String adminPassword,
        String cancellationReason
) {
}
