package com.RestaurantSystem.Entities.Customer.DTOs;

import java.util.UUID;

public record CalculateDistanceAndPriceDTO(
        UUID companyID,
        Double lat,
        Double lng
) {
}
