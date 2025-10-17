package com.RestaurantSystem.Entities.Shift.DTOs;

public record CloseShiftDTO(
        String companyID,
        String shiftID,
        String adminPassword
) {
}
