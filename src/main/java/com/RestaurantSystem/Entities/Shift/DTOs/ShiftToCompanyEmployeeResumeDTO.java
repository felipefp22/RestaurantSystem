package com.RestaurantSystem.Entities.Shift.DTOs;

public record ShiftToCompanyEmployeeResumeDTO(
        String id,
        String shiftNumber,
        String startTimeUTC,
        String endTimeUTC,
        String employeeOpenedShift,
        String managerWhoseOpenedShift

) {
}
