package com.RestaurantSystem.Entities.Customer.DTOs;

import java.util.UUID;

public record CreateOrUpdateCustomerDTO(
        UUID companyID,
        UUID id,
        String customerName,
        String phone,
        String address,
        String addressNumber,
        String city,
        String state,
        String zipCode,
        Double lat,
        Double lng,
        String complement
) {
}
