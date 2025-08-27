package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.List;
import java.util.UUID;

public record ProductsToAddOnOrderDTO(
        List<UUID> productsId
) {
}
