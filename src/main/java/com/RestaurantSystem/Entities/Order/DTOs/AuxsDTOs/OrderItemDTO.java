package com.RestaurantSystem.Entities.Order.DTOs.AuxsDTOs;

import java.util.List;

public record OrderItemDTO(
        List<String> productsIDs,
        List<String> productOptsIDs,
        String notes,
        String ifoodPdvCodeError,
        Double customPrice
) {
}
