package com.RestaurantSystem.Entities.Shift.DTOs;

import java.util.UUID;

public record CloseShiftDTO(
        UUID companyID,
        String shiftID,
        String adminPassword
) {
}
