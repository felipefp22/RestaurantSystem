package com.RestaurantSystem.Services.WebRequests.IFoodDTOs;

import java.util.UUID;

public record ReceiveCustomerCodeToRegisterIFoodDTO(
        UUID companyID,
        String code
) {
}
