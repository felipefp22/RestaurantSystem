package com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs;

import java.util.List;

public record OrderItemDTO(
        List<String> productsIDs,
        int quantity
) {
}
