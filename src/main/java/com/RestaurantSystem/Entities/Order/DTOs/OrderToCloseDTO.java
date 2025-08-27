package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record OrderToCloseDTO(
        UUID orderID,
        boolean clientSaidNoTax,
        Double discountValue
) {
}
