package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record ConfirmOrCancelOrderDTO(
        UUID orderID,
        String adminPassword
) {
}
