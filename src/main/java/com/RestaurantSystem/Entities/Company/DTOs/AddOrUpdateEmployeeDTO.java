package com.RestaurantSystem.Entities.Company.DTOs;

import java.util.UUID;

public record AddOrUpdateEmployeeDTO(
        UUID companyId,
        String employeeEmail,
        String position
) {
}
