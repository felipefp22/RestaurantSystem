package com.RestaurantSystem.Services.WebRequests.DTOs;

import java.util.UUID;

public record ReceiveCustomerCodeToRegisterIFoodDTO(
        UUID companyID,
        String code
) {
}
