package com.RestaurantSystem.Entities.DTOs;

import java.util.UUID;

public record OrderToCloseDTO(
        UUID orderID,
        boolean clientSaidNoTax,
        Double discountValue
) {
}
