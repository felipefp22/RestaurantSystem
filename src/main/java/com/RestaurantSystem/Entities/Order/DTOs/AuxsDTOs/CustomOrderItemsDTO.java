package com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs;

import java.util.List;

public record CustomOrderItemsDTO(
        List<String> productID,
        int quantity
) {
}