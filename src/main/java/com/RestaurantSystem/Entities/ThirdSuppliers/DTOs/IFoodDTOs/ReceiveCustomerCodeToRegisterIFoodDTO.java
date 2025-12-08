package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.IFoodDTOs;

import java.util.UUID;

public record ReceiveCustomerCodeToRegisterIFoodDTO(
        UUID companyID,
        String code
) {
}
