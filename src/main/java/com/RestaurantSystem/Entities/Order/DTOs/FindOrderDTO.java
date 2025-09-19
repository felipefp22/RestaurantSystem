package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record FindOrderDTO(
        UUID companyID,
        UUID orderID
) {
}
