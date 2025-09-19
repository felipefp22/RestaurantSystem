package com.RestaurantSystem.Entities.Customer.DTOs;

import java.util.UUID;

public record FindCustomerDTO(
        UUID companyID,
        UUID customerID
) {
}
